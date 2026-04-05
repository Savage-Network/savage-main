package com.minecraft.savagemc.hungergames.game.scheduler;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import com.minecraft.savagemc.hungergames.game.state.GameState;

/**
 * Tarefa de verificação do jogo HungerGames
 * Verifica estado do jogo e executa ações automáticas
 */
public class GameCheckTask implements Runnable {
    
    private final HungerGamesPlugin plugin;
    
    public GameCheckTask(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        if (plugin.getGameManager().getCurrentGame() == null) {
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        long stateTime = plugin.getGameManager().getCurrentGame().getStateTimeElapsed();
        
        // Verificar transições automáticas
        switch (currentState) {
            case WAITING:
                checkWaitingState();
                break;
            case STARTING:
                checkStartingState(stateTime);
                break;
            case INVINCIBILITY:
                checkInvincibilityState(stateTime);
                break;
            case PLAYING:
                checkPlayingState(stateTime);
                break;
            case FEAST:
                checkFeastState(stateTime);
                break;
            case FINAL_BATTLE:
                checkFinalBattleState(stateTime);
                break;
            case ENDING:
                checkEndingState(stateTime);
                break;
            case RESTARTING:
                checkRestartingState(stateTime);
                break;
        }
    }
    
    /**
     * Verifica estado de espera
     */
    private void checkWaitingState() {
        int playerCount = plugin.getGameManager().getCurrentGame().getPlayerCount();
        int minPlayers = plugin.getGameManager().getMinimumPlayers();
        
        // Auto-iniciar se tiver jogadores suficientes
        if (playerCount >= minPlayers && !plugin.getGameManager().getCurrentGame().isFull()) {
            // Enviar contagem regressiva
            if (playerCount >= minPlayers * 2) { // Se tiver o dobro do mínimo
                plugin.getServer().broadcastMessage("§e§lIniciando jogo em 10 segundos...");
                plugin.getGameManager().getCurrentGame().changeState(GameState.STARTING);
            }
        }
    }
    
    /**
     * Verifica estado de início
     */
    private void checkStartingState(long stateTime) {
        long remaining = 30000 - stateTime; // 30 segundos
        
        if (remaining <= 0) {
            plugin.getGameManager().getCurrentGame().changeState(GameState.INVINCIBILITY);
        } else if (remaining % 10000 == 0) { // A cada 10 segundos
            int seconds = (int) (remaining / 1000);
            plugin.getServer().broadcastMessage("§e§lO jogo começa em " + seconds + " segundos!");
        } else if (remaining <= 5000 && remaining % 1000 == 0) { // Últimos 5 segundos
            int seconds = (int) (remaining / 1000);
            plugin.getServer().broadcastMessage("§c§l" + seconds + "...");
        }
    }
    
    /**
     * Verifica estado de invencibilidade
     */
    private void checkInvincibilityState(long stateTime) {
        long remaining = 60000 - stateTime; // 1 minuto
        
        if (remaining <= 0) {
            plugin.getGameManager().getCurrentGame().changeState(GameState.PLAYING);
            plugin.getServer().broadcastMessage("§c§lA INVICIBILIDADE ACABOU! PvP LIBERADO!");
        } else if (remaining == 10000) { // 10 segundos antes
            plugin.getServer().broadcastMessage("§e§lInvencibilidade acaba em 10 segundos!");
        }
    }
    
    /**
     * Verifica estado de jogo
     */
    private void checkPlayingState(long stateTime) {
        // Verificar se deve iniciar feast (5 minutos)
        if (stateTime >= 300000 && !plugin.getGameManager().getCurrentGame().hasPlayerFeast()) {
            plugin.getGameManager().getCurrentGame().changeState(GameState.FEAST);
            return;
        }
        
        // Verificar se deve iniciar batalha final (15 minutos)
        if (stateTime >= 900000) {
            plugin.getGameManager().getCurrentGame().changeState(GameState.FINAL_BATTLE);
        }
        
        // Verificar se acabou o jogo (1 jogador ou menos)
        int alivePlayers = plugin.getGameManager().getCurrentGame().getPlayerCount();
        if (alivePlayers <= 1) {
            plugin.getGameManager().endGame();
        }
    }
    
    /**
     * Verifica estado de feast
     */
    private void checkFeastState(long stateTime) {
        long duration = 120000; // 2 minutos de feast
        
        if (stateTime >= duration) {
            // Voltar ao jogo normal após feast
            plugin.getGameManager().getCurrentGame().changeState(GameState.PLAYING);
            plugin.getServer().broadcastMessage("§eO feast terminou! O jogo continua!");
        }
    }
    
    /**
     * Verifica estado de batalha final
     */
    private void checkFinalBattleState(long stateTime) {
        // Reduzir borda gradualmente
        if (stateTime % 30000 == 0) { // A cada 30 segundos
            // Implementar redução de borda
            plugin.getServer().broadcastMessage("§c§lA borda está encolhendo!");
        }
        
        // Verificar se acabou o jogo
        int alivePlayers = plugin.getGameManager().getCurrentGame().getPlayerCount();
        if (alivePlayers <= 1) {
            plugin.getGameManager().endGame();
        }
    }
    
    /**
     * Verifica estado de finalização
     */
    private void checkEndingState(long stateTime) {
        long duration = 15000; // 15 segundos
        
        if (stateTime >= duration) {
            plugin.getGameManager().getCurrentGame().changeState(GameState.RESTARTING);
        }
    }
    
    /**
     * Verifica estado de reinício
     */
    private void checkRestartingState(long stateTime) {
        long duration = 10000; // 10 segundos
        
        if (stateTime >= duration) {
            // Reiniciar completamente
            plugin.getGameManager().restartGame();
        }
    }
}
