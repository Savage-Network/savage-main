package com.minecraft.savagemc.hungergames.game.scheduler;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;

/**
 * Tarefa de atualização de estatísticas do HungerGames
 * Salva dados e atualiza métricas periodicamente
 */
public class StatsUpdateTask implements Runnable {
    
    private final HungerGamesPlugin plugin;
    
    public StatsUpdateTask(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        try {
            // Salvar estatísticas dos usuários
            saveUserStats();
            
            // Atualizar estatísticas do servidor
            updateServerStats();
            
            // Salvar dados do jogo se houver um ativo
            saveGameData();
            
            // Enviar métricas para sistemas externos se necessário
            sendMetrics();
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error in stats update task: " + e.getMessage());
        }
    }
    
    /**
     * Salva estatísticas dos usuários
     */
    private void saveUserStats() {
        if (plugin.getUserManager() == null) {
            return;
        }
        
        int saved = 0;
        
        for (com.minecraft.savagemc.hungergames.user.User user : plugin.getUserManager().getOnlineUsers()) {
            try {
                user.save();
                saved++;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save stats for user: " + user.getPlayerUuid());
            }
        }
        
        if (saved > 0) {
            plugin.getLogger().info("Saved stats for " + saved + " users");
        }
    }
    
    /**
     * Atualiza estatísticas do servidor
     */
    private void updateServerStats() {
        // Coletar estatísticas atuais
        int onlinePlayers = plugin.getServer().getOnlinePlayers().size();
        int maxPlayers = plugin.getServer().getMaxPlayers();
        
        // Estatísticas do jogo atual
        int currentGamePlayers = 0;
        int currentSpectators = 0;
        String currentGameState = "NONE";
        
        if (plugin.getGameManager() != null && plugin.getGameManager().getCurrentGame() != null) {
            currentGamePlayers = plugin.getGameManager().getCurrentGame().getPlayerCount();
            currentSpectators = plugin.getGameManager().getCurrentGame().getSpectators().size();
            currentGameState = plugin.getGameManager().getCurrentGame().getState().toString();
        }
        
        // Estatísticas de arenas
        int totalArenas = 0;
        int availableArenas = 0;
        String currentArena = "NONE";
        
        if (plugin.getArenaManager() != null) {
            totalArenas = plugin.getArenaManager().getAllArenas().size();
            availableArenas = plugin.getArenaManager().getAvailableArenas().size();
            
            if (plugin.getArenaManager().getCurrentArena() != null) {
                currentArena = plugin.getArenaManager().getCurrentArena().getName();
            }
        }
        
        // Estatísticas de kits
        int totalKits = 0;
        int enabledKits = 0;
        
        if (plugin.getKitManager() != null && plugin.getKitManager().getKitRegistry() != null) {
            totalKits = plugin.getKitManager().getKitRegistry().getKitCount();
            enabledKits = plugin.getKitManager().getKitRegistry().getEnabledKitCount();
        }
        
        // Log das estatísticas
        plugin.getLogger().info(String.format(
            "Server Stats - Online: %d/%d, Game: %s (%d players, %d spectators), " +
            "Arena: %s (%d/%d available), Kits: %d/%d enabled",
            onlinePlayers, maxPlayers,
            currentGameState, currentGamePlayers, currentSpectators,
            currentArena, availableArenas, totalArenas,
            enabledKits, totalKits
        ));
    }
    
    /**
     * Salva dados do jogo atual
     */
    private void saveGameData() {
        if (plugin.getGameManager() == null || plugin.getGameManager().getCurrentGame() == null) {
            return;
        }
        
        try {
            // Salvar estado atual do jogo
            com.minecraft.savagemc.hungergames.game.instance.GameInstance game = plugin.getGameManager().getCurrentGame();
            
            // Atualizar tempo de último uso da arena
            if (plugin.getArenaManager() != null && plugin.getArenaManager().getCurrentArena() != null) {
                plugin.getArenaManager().getCurrentArena().updateLastUsed();
            }
            
            // Salvar dados da partida se necessário
            // Implementar salvamento em banco de dados
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save game data: " + e.getMessage());
        }
    }
    
    /**
     * Envia métricas para sistemas externos
     */
    private void sendMetrics() {
        // Enviar métricas para sistemas de monitoramento se configurado
        // Isso pode incluir envio para APIs externas, databases, etc.
        
        try {
            // Exemplo: enviar para sistema de analytics
            // analyticsService.sendMetrics(collectMetrics());
            
        } catch (Exception e) {
            // Não logar erros de métricas para não poluir logs
            // plugin.getLogger().fine("Failed to send metrics: " + e.getMessage());
        }
    }
    
    /**
     * Coleta métricas para envio
     */
    private java.util.Map<String, Object> collectMetrics() {
        java.util.Map<String, Object> metrics = new java.util.HashMap<>();
        
        // Métricas básicas
        metrics.put("timestamp", System.currentTimeMillis());
        metrics.put("online_players", plugin.getServer().getOnlinePlayers().size());
        metrics.put("max_players", plugin.getServer().getMaxPlayers());
        metrics.put("plugin_version", "1.0.0-SNAPSHOT");
        metrics.put("server_version", plugin.getServer().getVersion());
        
        // Métricas do jogo
        if (plugin.getGameManager() != null && plugin.getGameManager().getCurrentGame() != null) {
            metrics.put("game_state", plugin.getGameManager().getCurrentGame().getState().toString());
            metrics.put("game_players", plugin.getGameManager().getCurrentGame().getPlayerCount());
            metrics.put("game_spectators", plugin.getGameManager().getCurrentGame().getSpectators().size());
            metrics.put("game_time", plugin.getGameManager().getCurrentGame().getGameTimeElapsed());
        }
        
        // Métricas de performance
        Runtime runtime = Runtime.getRuntime();
        metrics.put("memory_used", runtime.totalMemory() - runtime.freeMemory());
        metrics.put("memory_max", runtime.maxMemory());
        metrics.put("memory_usage_percent", (double)(runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory() * 100);
        
        return metrics;
    }
}
