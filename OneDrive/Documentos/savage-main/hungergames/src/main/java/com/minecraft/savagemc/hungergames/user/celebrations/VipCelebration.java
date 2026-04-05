package com.minecraft.savagemc.hungergames.user.celebrations;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import org.bukkit.entity.Player;
import org.bukkit.Particle;

/**
 * Celebração VIP para vencedores do HungerGames
 * Efeitos mais elaborados e duradouros
 */
public class VipCelebration extends Celebration {
    
    public VipCelebration(HungerGamesPlugin plugin) {
        super(plugin, "vip");
    }
    
    @Override
    protected void onStart(Player player) {
        // Iniciar efeitos VIP
        startParticleEffects(player);
        startFireworks(player);
        playSounds(player);
        
        // Mensagem VIP
        broadcastMessage("§6§lVIP! §eParabéns a " + player.getName() + " por vencer o HungerGames com estilo!");
        
        // Dar efeito especial ao jogador
        applySpecialEffects(player);
    }
    
    @Override
    protected void onStop() {
        // Remover efeitos especiais
    }
    
    /**
     * Inicia efeitos de partículas VIP
     */
    private void startParticleEffects(Player player) {
        // Múltiplos círculos de partículas
        scheduleTask(() -> {
            if (!isActive()) return;
            
            // Círculo externo - partículas de fogo
            createParticleCircle(player, Particle.FLAME, 3.0, 24);
            
            // Círculo médio - partículas douradas
            createParticleCircle(player, Particle.FIREWORKS_SPARK, 2.0, 16);
            
            // Círculo interno - partículas brilhantes
            createParticleCircle(player, Particle.VILLAGER_HAPPY, 1.0, 12);
            
            // Espiral ascendente
            createParticleSpiral(player, Particle.ENCHANTMENT_TABLE, 1.5, 12);
            
        }, 5L); // A cada 0.25 segundos (mais frequente)
    }
    
    /**
     * Inicia fireworks VIP
     */
    private void startFireworks(Player player) {
        // Lançar fireworks em padrão VIP
        launchFirework(player, java.awt.Color.GOLD, java.awt.Color.WHITE);
        
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            launchFirework(player, java.awt.Color.CYAN, java.awt.Color.WHITE);
        }, 10L);
        
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            launchFirework(player, java.awt.Color.PURPLE, java.awt.Color.GOLD);
        }, 20L);
        
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            launchFirework(player, java.awt.Color.RED, java.awt.Color.GOLD);
        }, 30L);
        
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            launchFirework(player, java.awt.Color.GREEN, java.awt.Color.GOLD);
        }, 40L);
        
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            launchFirework(player, java.awt.Color.ORANGE, java.awt.Color.YELLOW);
        }, 50L);
        
        // Grand finale
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            
            // Múltiplos fireworks simultâneos
            for (int i = 0; i < 3; i++) {
                scheduleDelayedTask(() -> {
                    if (isActive()) {
                        launchFirework(player, java.awt.Color.RAINBOW.getRGB(), java.awt.Color.GOLD);
                    }
                }, i * 5L);
            }
        }, 70L);
    }
    
    /**
     * Toca sons VIP
     */
    private void playSounds(Player player) {
        // Som de level up mais alto
        playSound(player, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.5f, 1.2f);
        
        // Melodia VIP
        final int[] notes = {0, 2, 4, 5, 7, 9, 11, 12}; // Escala maior
        final float[] pitches = {1.0f, 1.122f, 1.26f, 1.335f, 1.5f, 1.682f, 1.888f, 2.0f};
        
        for (int i = 0; i < notes.length; i++) {
            final int noteIndex = i;
            scheduleDelayedTask(() -> {
                if (!isActive()) return;
                playSound(player, org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, pitches[noteIndex]);
            }, i * 3L);
        }
        
        // Sons periódicos VIP
        scheduleTask(() -> {
            if (!isActive()) return;
            playSound(player, org.bukkit.Sound.BLOCK_NOTE_BLOCK_BELL, 0.6f, 2.0f);
        }, 20L);
        
        // Som final VIP
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            playSound(player, org.bukkit.Sound.ENTITY_WITHER_DEATH, 0.5f, 0.5f);
        }, 100L);
    }
    
    /**
     * Aplica efeitos especiais ao jogador
     */
    private void applySpecialEffects(Player player) {
        // Dar efeito de brilho
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.GLOWING, 
            200, // 10 segundos
            0, 
            false, 
            false, 
            false
        ));
        
        // Dar efeito de velocidade
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.SPEED, 
            200, // 10 segundos
            1, // Speed II
            false, 
            false, 
            false
        ));
        
        // Dar efeito de salto
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.JUMP, 
            200, // 10 segundos
            1, // Jump II
            false, 
            false, 
            false
        ));
    }
}
