package com.minecraft.savagemc.hungergames.user.celebrations;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import org.bukkit.entity.Player;
import org.bukkit.Particle;

/**
 * Celebração padrão para vencedores do HungerGames
 * Efeitos simples de partículas e fireworks
 */
public class DefaultCelebration extends Celebration {
    
    public DefaultCelebration(HungerGamesPlugin plugin) {
        super(plugin, "default");
    }
    
    @Override
    protected void onStart(Player player) {
        // Iniciar efeitos visuais
        startParticleEffects(player);
        startFireworks(player);
        playSounds(player);
        
        // Mensagem de celebração
        broadcastMessage("§eParabéns a " + player.getName() + " por vencer o HungerGames!");
    }
    
    @Override
    protected void onStop() {
        // Limpar efeitos se necessário
    }
    
    /**
     * Inicia efeitos de partículas
     */
    private void startParticleEffects(Player player) {
        // Task periódica para partículas
        scheduleTask(() -> {
            if (!isActive()) return;
            
            // Círculo de partículas douradas
            createParticleCircle(player, Particle.FIREWORKS_SPARK, 2.0, 16);
            
            // Partículas ascendentes
            createParticleSpiral(player, Particle.VILLAGER_HAPPY, 1.5, 8);
            
        }, 10L); // A cada 0.5 segundos
    }
    
    /**
     * Inicia fireworks
     */
    private void startFireworks(Player player) {
        // Lançar fireworks imediatamente
        launchFirework(player, java.awt.Color.GOLD, java.awt.Color.YELLOW);
        
        // Schedule mais fireworks
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            launchFirework(player, java.awt.Color.RED, java.awt.Color.ORANGE);
        }, 20L); // 1 segundo depois
        
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            launchFirework(player, java.awt.Color.GREEN, java.awt.Color.BLUE);
        }, 40L); // 2 segundos depois
        
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            launchFirework(player, java.awt.Color.PURPLE, java.awt.Color.PINK);
        }, 60L); // 3 segundos depois
    }
    
    /**
     * Toca sons de celebração
     */
    private void playSounds(Player player) {
        // Som de level up imediato
        playSound(player, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        
        // Sons periódicos
        scheduleTask(() -> {
            if (!isActive()) return;
            playSound(player, org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2.0f);
        }, 30L); // A cada 1.5 segundos
        
        // Som final
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            playSound(player, org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);
        }, 80L); // 4 segundos depois
    }
}
