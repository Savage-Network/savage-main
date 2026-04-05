package com.minecraft.savagemc.hungergames.game.scheduler;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;

/**
 * Tarefa de limpeza do HungerGames
 * Remove entidades desnecessárias e otimiza performance
 */
public class CleanupTask implements Runnable {
    
    private final HungerGamesPlugin plugin;
    
    public CleanupTask(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        try {
            // Limpar entidades desnecessárias
            cleanupEntities();
            
            // Limpar jogos finalizados
            cleanupFinishedGames();
            
            // Limpar usuários desconectados
            cleanupDisconnectedUsers();
            
            // Otimizar memória
            optimizeMemory();
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error in cleanup task: " + e.getMessage());
        }
    }
    
    /**
     * Limpa entidades desnecessárias
     */
    private void cleanupEntities() {
        if (plugin.getServer().getWorlds().isEmpty()) {
            return;
        }
        
        org.bukkit.World world = plugin.getServer().getWorlds().get(0);
        
        int removed = 0;
        
        // Remover itens dropados antigos
        for (org.bukkit.entity.Item item : world.getEntitiesByClass(org.bukkit.entity.Item.class)) {
            if (item.getTicksLived() > 6000) { // Mais de 5 minutos
                item.remove();
                removed++;
            }
        }
        
        // Remover flechas e projéteis antigos
        for (org.bukkit.entity.Entity entity : world.getEntities()) {
            if (entity instanceof org.bukkit.entity.Arrow || 
                entity instanceof org.bukkit.entity.Snowball ||
                entity instanceof org.bukkit.entity.Egg) {
                
                if (entity.getTicksLived() > 1200) { // Mais de 1 minuto
                    entity.remove();
                    removed++;
                }
            }
        }
        
        // Remover experience orbs antigos
        for (org.bukkit.entity.ExperienceOrb orb : world.getEntitiesByClass(org.bukkit.entity.ExperienceOrb.class)) {
            if (orb.getTicksLived() > 6000) { // Mais de 5 minutos
                orb.remove();
                removed++;
            }
        }
        
        if (removed > 0) {
            plugin.getLogger().info("Cleaned up " + removed + " entities");
        }
    }
    
    /**
     * Limpa jogos finalizados
     */
    private void cleanupFinishedGames() {
        if (plugin.getGameManager() != null) {
            plugin.getGameManager().cleanupFinishedGames();
        }
    }
    
    /**
     * Limpa usuários desconectados
     */
    private void cleanupDisconnectedUsers() {
        if (plugin.getUserManager() == null) {
            return;
        }
        
        int removed = 0;
        
        // Verificar usuários online vs jogadores Bukkit online
        for (com.minecraft.savagemc.hungergames.user.User user : plugin.getUserManager().getOnlineUsers()) {
            org.bukkit.entity.Player player = plugin.getServer().getPlayer(user.getPlayerUuid());
            
            if (player == null || !player.isOnline()) {
                // Usuário marcado como online mas não está no Bukkit
                plugin.getUserManager().unloadUser(user.getPlayerUuid());
                removed++;
            }
        }
        
        if (removed > 0) {
            plugin.getLogger().info("Cleaned up " + removed + " disconnected users");
        }
    }
    
    /**
     * Otimiza uso de memória
     */
    private void optimizeMemory() {
        // Forçar garbage collection se necessário
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        
        double usagePercent = (double) usedMemory / maxMemory * 100;
        
        if (usagePercent > 80) { // Se estiver usando mais de 80% da memória
            plugin.getLogger().warning("High memory usage: " + String.format("%.1f%%", usagePercent));
            
            // Sugerir garbage collection
            System.gc();
            
            plugin.getLogger().info("Garbage collection suggested");
        }
        
        // Limpar caches se necessário
        if (usagePercent > 90) {
            plugin.getLogger().warning("Very high memory usage, clearing caches...");
            
            // Limpar caches de kits se existirem
            if (plugin.getKitManager() != null) {
                // Implementar limpeza de cache de kits se necessário
            }
        }
    }
}
