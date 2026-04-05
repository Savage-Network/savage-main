package com.minecraft.savagemc.hungergames.user.celebrations;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import org.bukkit.entity.Player;
import org.bukkit.Particle;

/**
 * Celebração especial para vencedores do HungerGames
 * Efeitos espetaculares e exclusivos
 */
public class SpecialCelebration extends Celebration {
    
    public SpecialCelebration(HungerGamesPlugin plugin) {
        super(plugin, "special");
    }
    
    @Override
    protected void onStart(Player player) {
        // Iniciar efeitos especiais
        startParticleEffects(player);
        startFireworks(player);
        playSounds(player);
        
        // Mensagem especial
        broadcastMessage("§6§l✨ ESPECIAL! ✨ §eParabéns a " + player.getName() + " por uma vitória lendária!");
        
        // Aplicar efeitos épicos
        applyEpicEffects(player);
    }
    
    @Override
    protected void onStop() {
        // Limpar efeitos épicos
    }
    
    /**
     * Inicia efeitos de partículas especiais
     */
    private void startParticleEffects(Player player) {
        // Efeitos complexos e múltiplos
        scheduleTask(() -> {
            if (!isActive()) return;
            
            // Círculos concêntricos
            for (int i = 1; i <= 3; i++) {
                createParticleCircle(player, Particle.FIREWORKS_SPARK, i * 1.5, 16 * i);
            }
            
            // Espiral dupla
            createParticleSpiral(player, Particle.ENCHANTMENT_TABLE, 2.0, 16);
            createParticleSpiral(player, Particle.VILLAGER_HAPPY, 1.5, 12);
            
            // Partículas aleatórias ao redor
            createRandomParticles(player);
            
        }, 3L); // Frequência muito alta
    }
    
    /**
     * Cria partículas aleatórias ao redor do jogador
     */
    private void createRandomParticles(Player player) {
        org.bukkit.Location center = player.getLocation().add(0, 1, 0);
        
        for (int i = 0; i < 8; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double radius = 1 + Math.random() * 2;
            double height = Math.random() * 2;
            
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            org.bukkit.Location particleLoc = center.clone().add(x, height, z);
            
            // Partículas aleatórias
            Particle[] particles = {
                Particle.FLAME, Particle.SMOKE_NORMAL, Particle.SPELL_WITCH,
                Particle.PORTAL, Particle.END_ROD, Particle.CRIT
            };
            
            Particle particle = particles[(int) (Math.random() * particles.length)];
            player.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0.1);
        }
    }
    
    /**
     * Inicia fireworks especiais
     */
    private void startFireworks(Player player) {
        // Sequência espetacular de fireworks
        launchFirework(player, java.awt.Color.GOLD, java.awt.Color.WHITE);
        
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            launchFirework(player, java.awt.Color.RED, java.awt.Color.BLUE);
        }, 5L);
        
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            launchFirework(player, java.awt.Color.GREEN, java.awt.Color.YELLOW);
        }, 10L);
        
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            launchFirework(player, java.awt.Color.PURPLE, java.awt.Color.CYAN);
        }, 15L);
        
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            launchFirework(player, java.awt.Color.ORANGE, java.awt.Color.PINK);
        }, 20L);
        
        // Sequência rápida
        for (int i = 0; i < 10; i++) {
            scheduleDelayedTask(() -> {
                if (!isActive()) return;
                launchFirework(player, getRandomColor(), getRandomColor());
            }, 25L + i * 2L);
        }
        
        // Grand finale espetacular
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            
            // Múltiplos fireworks em círculo
            for (int i = 0; i < 8; i++) {
                final double angle = (2 * Math.PI * i) / 8;
                final double x = Math.cos(angle) * 3;
                final double z = Math.sin(angle) * 3;
                
                scheduleDelayedTask(() -> {
                    if (isActive()) {
                        org.bukkit.Location loc = player.getLocation().add(x, 5, z);
                        org.bukkit.Firework firework = (org.bukkit.Firework) player.getWorld().spawnEntity(loc, org.bukkit.EntityType.FIREWORK);
                        
                        org.bukkit.inventory.meta.FireworkMeta meta = (org.bukkit.inventory.meta.FireworkMeta) firework.getFireworkMeta();
                        meta.addEffect(org.bukkit.FireworkEffect.builder()
                                .with(org.bukkit.FireworkEffect.Type.STAR)
                                .withColor(java.awt.Color.RAINBOW)
                                .withFlicker()
                                .withTrail()
                                .build());
                        meta.setPower(2);
                        firework.setFireworkMeta(meta);
                        
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, firework::remove, 30L);
                    }
                }, i * 2L);
            }
        }, 50L);
    }
    
    /**
     * Obtém uma cor aleatória
     */
    private java.awt.Color getRandomColor() {
        java.awt.Color[] colors = {
            java.awt.Color.RED, java.awt.Color.GREEN, java.awt.Color.BLUE,
            java.awt.Color.YELLOW, java.awt.Color.ORANGE, java.awt.Color.PINK,
            java.awt.Color.CYAN, java.awt.Color.MAGENTA, java.awt.Color.WHITE
        };
        
        return colors[(int) (Math.random() * colors.length)];
    }
    
    /**
     * Toca sons especiais
     */
    private void playSounds(Player player) {
        // Som épico inicial
        playSound(player, org.bukkit.Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
        
        // Melodia especial mais complexa
        final String[] sounds = {
            "BLOCK_NOTE_BLOCK_PLING", "BLOCK_NOTE_BLOCK_BASS", "BLOCK_NOTE_BLOCK_BELL",
            "BLOCK_NOTE_BLOCK_FLUTE", "BLOCK_NOTE_BLOCK_GUITAR", "BLOCK_NOTE_BLOCK_CHIME"
        };
        
        for (int i = 0; i < 16; i++) {
            final int noteIndex = i;
            scheduleDelayedTask(() -> {
                if (!isActive()) return;
                
                String soundName = sounds[noteIndex % sounds.length];
                try {
                    org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundName);
                    playSound(player, sound, 0.7f, 0.8f + (noteIndex * 0.05f));
                } catch (IllegalArgumentException e) {
                    // Ignorar sons inválidos
                }
            }, i * 2L);
        }
        
        // Sons ambientes especiais
        scheduleTask(() -> {
            if (!isActive()) return;
            playSound(player, org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.3f, 0.5f);
        }, 40L);
        
        // Som final épico
        scheduleDelayedTask(() -> {
            if (!isActive()) return;
            playSound(player, org.bukkit.Sound.ENTITY_ENDER_DRAGON_DEATH, 0.5f, 0.3f);
        }, 120L);
    }
    
    /**
     * Aplica efeitos épicos ao jogador
     */
    private void applyEpicEffects(Player player) {
        // Efeitos de poção poderosos
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.GLOWING, 
            300, // 15 segundos
            0, 
            false, 
            false, 
            false
        ));
        
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.SPEED, 
            300, // 15 segundos
            2, // Speed III
            false, 
            false, 
            false
        ));
        
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.JUMP, 
            300, // 15 segundos
            2, // Jump III
            false, 
            false, 
            false
        ));
        
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.NIGHT_VISION, 
            600, // 30 segundos
            0, 
            false, 
            false, 
            false
        ));
        
        // Dar itens especiais se for configurado
        giveSpecialItems(player);
    }
    
    /**
     * Dá itens especiais ao vencedor
     */
    private void giveSpecialItems(Player player) {
        // Dar uma maçã dourada especial
        org.bukkit.inventory.ItemStack goldenApple = new org.bukkit.inventory.ItemStack(
            org.bukkit.Material.GOLDEN_APPLE, 1
        );
        
        org.bukkit.inventory.meta.ItemMeta meta = goldenApple.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lMaçã do Vencedor");
            meta.setLore(java.util.Arrays.asList(
                "§7Uma maçã mágica concedida",
                "§7aos verdadeiros campeões!",
                "§eEspecial: §fVencedor do HungerGames"
            ));
            goldenApple.setItemMeta(meta);
        }
        
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(goldenApple);
        } else {
            player.getWorld().dropItem(player.getLocation(), goldenApple);
        }
    }
}
