package com.minecraft.savagemc.hungergames.game.instance;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import com.minecraft.savagemc.hungergames.game.state.GameState;
import com.minecraft.savagemc.hungergames.game.state.GameStateManager;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Instância de um jogo HungerGames individual
 * Gerencia jogadores, estado e ciclo de vida de uma partida
 */
@Getter
public class GameInstance {
    
    private final HungerGamesPlugin plugin;
    private final UUID gameId;
    private final GameStateManager stateManager;
    private final Set<UUID> players;
    private final Set<UUID> spectators;
    private final ConcurrentHashMap<UUID, GamePlayer> gamePlayers;
    
    private GameState state;
    private long startTime;
    private long stateStartTime;
    private int taskId;
    
    public GameInstance(HungerGamesPlugin plugin, UUID gameId) {
        this.plugin = plugin;
        this.gameId = gameId;
        this.stateManager = new GameStateManager();
        this.players = ConcurrentHashMap.newKeySet();
        this.spectators = ConcurrentHashMap.newKeySet();
        this.gamePlayers = new ConcurrentHashMap<>();
        
        this.state = GameState.WAITING;
        this.startTime = System.currentTimeMillis();
        this.stateStartTime = System.currentTimeMillis();
        this.taskId = -1;
    }
    
    /**
     * Inicia o jogo
     */
    public boolean start() {
        if (!stateManager.isValidTransition(state, GameState.STARTING)) {
            return false;
        }
        
        changeState(GameState.STARTING);
        scheduleStateTransition();
        return true;
    }
    
    /**
     * Finaliza o jogo
     */
    public void end() {
        changeState(GameState.ENDING);
        scheduleStateTransition();
    }
    
    /**
     * Força finalização imediata
     */
    public void forceEnd() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        
        changeState(GameState.RESTARTING);
        cleanup();
    }
    
    /**
     * Reinicia o jogo
     */
    public void restart() {
        end();
    }
    
    /**
     * Adiciona um jogador ao jogo
     */
    public boolean addPlayer(UUID playerUuid) {
        if (!state.allowsJoin()) {
            return false;
        }
        
        if (players.size() >= plugin.getGameManager().getMaximumPlayers()) {
            return false;
        }
        
        if (players.contains(playerUuid)) {
            return false;
        }
        
        players.add(playerUuid);
        gamePlayers.put(playerUuid, new GamePlayer(playerUuid));
        
        // Verificar se pode iniciar automaticamente
        checkAutoStart();
        
        return true;
    }
    
    /**
     * Remove um jogador do jogo
     */
    public boolean removePlayer(UUID playerUuid) {
        if (!players.contains(playerUuid)) {
            return false;
        }
        
        players.remove(playerUuid);
        gamePlayers.remove(playerUuid);
        
        // Se o jogo estiver em andamento e não houver jogadores suficientes, finalizar
        if (state.isPlaying() && players.size() < 2) {
            end();
        }
        
        return true;
    }
    
    /**
     * Adiciona um espectador
     */
    public boolean addSpectator(UUID playerUuid) {
        if (players.contains(playerUuid)) {
            return false; // Jogador não pode ser espectador ao mesmo tempo
        }
        
        spectators.add(playerUuid);
        return true;
    }
    
    /**
     * Remove um espectador
     */
    public boolean removeSpectator(UUID playerUuid) {
        return spectators.remove(playerUuid);
    }
    
    /**
     * Verifica se um jogador está no jogo
     */
    public boolean hasPlayer(UUID playerUuid) {
        return players.contains(playerUuid);
    }
    
    /**
     * Verifica se um jogador é espectador
     */
    public boolean isSpectator(UUID playerUuid) {
        return spectators.contains(playerUuid);
    }
    
    /**
     * Obtém o número de jogadores
     */
    public int getPlayerCount() {
        return players.size();
    }
    
    /**
     * Verifica se o jogo está cheio
     */
    public boolean isFull() {
        return players.size() >= plugin.getGameManager().getMaximumPlayers();
    }
    
    /**
     * Verifica se o jogo está vazio
     */
    public boolean isEmpty() {
        return players.isEmpty();
    }
    
    /**
     * Obtém um jogador do jogo
     */
    public GamePlayer getGamePlayer(UUID playerUuid) {
        return gamePlayers.get(playerUuid);
    }
    
    /**
     * Altera o estado do jogo
     */
    public void changeState(GameState newState) {
        if (!stateManager.isValidTransition(state, newState)) {
            plugin.getLogger().warning("Invalid state transition: " + state + " -> " + newState);
            return;
        }
        
        GameState oldState = state;
        state = newState;
        stateStartTime = System.currentTimeMillis();
        
        plugin.getLogger().info("Game " + gameId + " state changed: " + oldState + " -> " + newState);
        
        // Notificar mudança de estado
        onStateChange(oldState, newState);
    }
    
    /**
     * Agenda transição automática de estado
     */
    private void scheduleStateTransition() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
        
        long duration = stateManager.getStateDuration(state) * 20L; // Convert to ticks
        
        if (duration > 0 && stateManager.allowsAutoTransition(state)) {
            taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(
                plugin,
                this::onStateTimeout,
                duration
            );
        }
    }
    
    /**
     * Chamado quando o tempo do estado expira
     */
    private void onStateTimeout() {
        GameState nextState = stateManager.getNextState(state);
        
        // Lógica especial para transições
        if (state == GameState.FEAST) {
            // Após feast, voltar ao jogo normal
            nextState = GameState.PLAYING;
        }
        
        changeState(nextState);
        scheduleStateTransition();
    }
    
    /**
     * Verifica se deve iniciar automaticamente
     */
    private void checkAutoStart() {
        if (state == GameState.WAITING && players.size() >= plugin.getGameManager().getMinimumPlayers()) {
            start();
        }
    }
    
    /**
     * Chamado quando o estado muda
     */
    private void onStateChange(GameState oldState, GameState newState) {
        // Lógica específica para cada mudança de estado
        switch (newState) {
            case STARTING:
                onGameStarting();
                break;
            case INVINCIBILITY:
                onInvincibilityStart();
                break;
            case PLAYING:
                onGameStart();
                break;
            case FEAST:
                onFeastStart();
                break;
            case FINAL_BATTLE:
                onFinalBattleStart();
                break;
            case ENDING:
                onGameEnd();
                break;
            case RESTARTING:
                onGameRestart();
                break;
        }
    }
    
    /**
     * Chamado quando o jogo está iniciando
     */
    private void onGameStarting() {
        // Teleportar jogadores para posições iniciais
        // Enviar mensagens de contagem regressiva
    }
    
    /**
     * Chamado quando a invencibilidade começa
     */
    private void onInvincibilityStart() {
        // Aplicar efeito de invencibilidade
        // Preparar arena
    }
    
    /**
     * Chamado quando o jogo realmente começa
     */
    private void onGameStart() {
        startTime = System.currentTimeMillis();
        // Remover invencibilidade
        // Permitir dano
        // Iniciar feast timer
    }
    
    /**
     * Chamado quando o feast começa
     */
    private void onFeastStart() {
        // Gerar feast
        // Notificar jogadores
    }
    
    /**
     * Chamado quando a batalha final começa
     */
    private void onFinalBattleStart() {
        // Reduzir área de jogo
        // Notificar jogadores
    }
    
    /**
     * Chamado quando o jogo termina
     */
    private void onGameEnd() {
        // Celebrar vencedor
        // Mostrar estatísticas
        // Preparar para reinício
    }
    
    /**
     * Chamado quando o jogo reinicia
     */
    private void onGameRestart() {
        cleanup();
    }
    
    /**
     * Limpa o jogo
     */
    private void cleanup() {
        players.clear();
        spectators.clear();
        gamePlayers.clear();
        
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
    
    /**
     * Obtém o tempo decorrido no estado atual
     */
    public long getStateTimeElapsed() {
        return System.currentTimeMillis() - stateStartTime;
    }
    
    /**
     * Obtém o tempo total de jogo
     */
    public long getGameTimeElapsed() {
        return System.currentTimeMillis() - startTime;
    }
}
