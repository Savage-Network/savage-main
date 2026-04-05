package com.minecraft.savagemc.hungergames.game;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import com.minecraft.savagemc.hungergames.game.instance.GameInstance;
import com.minecraft.savagemc.hungergames.game.state.GameState;
import com.minecraft.savagemc.hungergames.game.state.GameStateManager;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerenciador principal de jogos do HungerGames
 * Controla múltiplas instâncias de jogo e estados
 */
@Getter
public class GameManager {

    private final HungerGamesPlugin plugin;
    private final GameStateManager stateManager;
    private final ConcurrentHashMap<UUID, GameInstance> gameInstances;
    private GameInstance currentGame;

    public GameManager(HungerGamesPlugin plugin) {
        this.plugin = plugin;
        this.stateManager = new GameStateManager();
        this.gameInstances = new ConcurrentHashMap<>();
    }

    /**
     * Inicializa o gerenciador de jogos
     */
    public void initialize() {
        plugin.getLogger().info("Game Manager initialized");
        
        // Registrar estados do jogo
        registerGameStates();
        
        // Iniciar primeira instância de jogo
        createNewGame();
    }

    /**
     * Registra todos os estados possíveis do jogo
     */
    private void registerGameStates() {
        stateManager.registerState(GameState.WAITING);
        stateManager.registerState(GameState.STARTING);
        stateManager.registerState(GameState.INVINCIBILITY);
        stateManager.registerState(GameState.PLAYING);
        stateManager.registerState(GameState.FEAST);
        stateManager.registerState(GameState.FINAL_BATTLE);
        stateManager.registerState(GameState.ENDING);
        stateManager.registerState(GameState.RESTARTING);
    }

    /**
     * Cria uma nova instância de jogo
     */
    public GameInstance createNewGame() {
        UUID gameId = UUID.randomUUID();
        GameInstance game = new GameInstance(plugin, gameId);
        
        gameInstances.put(gameId, game);
        currentGame = game;
        
        plugin.getLogger().info("Created new game instance: " + gameId);
        return game;
    }

    /**
     * Obtém a instância de jogo atual
     */
    public GameInstance getCurrentGame() {
        return currentGame;
    }

    /**
     * Define a instância de jogo atual
     */
    public void setCurrentGame(GameInstance game) {
        this.currentGame = game;
    }

    /**
     * Inicia o jogo atual
     */
    public boolean startGame() {
        if (currentGame == null) {
            plugin.getLogger().warning("No current game instance to start");
            return false;
        }

        if (currentGame.getPlayerCount() < getMinimumPlayers()) {
            plugin.getLogger().info("Not enough players to start game. Required: " + getMinimumPlayers() + ", Current: " + currentGame.getPlayerCount());
            return false;
        }

        return currentGame.start();
    }

    /**
     * Finaliza o jogo atual
     */
    public void endGame() {
        if (currentGame != null) {
            currentGame.end();
        }
    }

    /**
     * Reinicia o jogo atual
     */
    public void restartGame() {
        if (currentGame != null) {
            currentGame.restart();
        }
        
        // Criar nova instância após reinício
        createNewGame();
    }

    /**
     * Obtém o número mínimo de jogadores para iniciar
     */
    public int getMinimumPlayers() {
        return plugin.getConfigManager().getConfigInt("game.minimum-players", 2);
    }

    /**
     * Obtém o número máximo de jogadores
     */
    public int getMaximumPlayers() {
        return plugin.getConfigManager().getConfigInt("game.maximum-players", 24);
    }

    /**
     * Verifica se o jogo pode iniciar
     */
    public boolean canStartGame() {
        return currentGame != null && 
               currentGame.getState() == GameState.WAITING && 
               currentGame.getPlayerCount() >= getMinimumPlayers();
    }

    /**
     * Adiciona um jogador ao jogo atual
     */
    public boolean addPlayer(UUID playerUuid) {
        if (currentGame == null) {
            plugin.getLogger().warning("No current game instance available");
            return false;
        }

        if (currentGame.isFull()) {
            plugin.getLogger().info("Game is full: " + currentGame.getPlayerCount() + "/" + getMaximumPlayers());
            return false;
        }

        return currentGame.addPlayer(playerUuid);
    }

    /**
     * Remove um jogador do jogo atual
     */
    public boolean removePlayer(UUID playerUuid) {
        if (currentGame == null) {
            return false;
        }

        return currentGame.removePlayer(playerUuid);
    }

    /**
     * Verifica se um jogador está em algum jogo
     */
    public boolean isPlayerInGame(UUID playerUuid) {
        return currentGame != null && currentGame.hasPlayer(playerUuid);
    }

    /**
     * Obtém a instância de jogo de um jogador
     */
    public GameInstance getPlayerGame(UUID playerUuid) {
        return isPlayerInGame(playerUuid) ? currentGame : null;
    }

    /**
     * Limpa jogos finalizados
     */
    public void cleanupFinishedGames() {
        gameInstances.entrySet().removeIf(entry -> {
            GameInstance game = entry.getValue();
            return game.getState() == GameState.RESTARTING;
        });
    }

    /**
     * Desliga o gerenciador de jogos
     */
    public void shutdown() {
        plugin.getLogger().info("Shutting down Game Manager...");
        
        // Finalizar todos os jogos ativos
        gameInstances.values().forEach(GameInstance::forceEnd);
        gameInstances.clear();
        
        currentGame = null;
        
        plugin.getLogger().info("Game Manager shut down");
    }

    /**
     * Obtém estatísticas do gerenciador
     */
    public String getStats() {
        return String.format(
            "GameManager Stats - Active Games: %d, Current Game Players: %d/%d, State: %s",
            gameInstances.size(),
            currentGame != null ? currentGame.getPlayerCount() : 0,
            getMaximumPlayers(),
            currentGame != null ? currentGame.getState() : "NONE"
        );
    }
}
