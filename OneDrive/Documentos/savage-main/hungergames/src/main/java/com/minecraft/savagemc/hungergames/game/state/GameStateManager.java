package com.minecraft.savagemc.hungergames.game.state;

import java.util.EnumSet;
import java.util.Set;

/**
 * Gerenciador de estados do jogo
 * Controla as transições entre estados e validações
 */
public class GameStateManager {
    
    private final Set<GameState> registeredStates;
    
    public GameStateManager() {
        this.registeredStates = EnumSet.allOf(GameState.class);
    }
    
    /**
     * Registra um estado do jogo
     */
    public void registerState(GameState state) {
        registeredStates.add(state);
    }
    
    /**
     * Verifica se uma transição de estado é válida
     */
    public boolean isValidTransition(GameState from, GameState to) {
        if (from == null) return to == GameState.WAITING;
        if (to == null) return false;
        
        switch (from) {
            case WAITING:
                return to == GameState.STARTING || to == GameState.RESTARTING;
                
            case STARTING:
                return to == GameState.INVINCIBILITY || to == GameState.WAITING || to == GameState.RESTARTING;
                
            case INVINCIBILITY:
                return to == GameState.PLAYING || to == GameState.RESTARTING;
                
            case PLAYING:
                return to == GameState.FEAST || to == GameState.FINAL_BATTLE || to == GameState.ENDING || to == GameState.RESTARTING;
                
            case FEAST:
                return to == GameState.PLAYING || to == GameState.FINAL_BATTLE || to == GameState.ENDING || to == GameState.RESTARTING;
                
            case FINAL_BATTLE:
                return to == GameState.ENDING || to == GameState.RESTARTING;
                
            case ENDING:
                return to == GameState.RESTARTING;
                
            case RESTARTING:
                return to == GameState.WAITING;
                
            default:
                return false;
        }
    }
    
    /**
     * Verifica se um estado está registrado
     */
    public boolean isStateRegistered(GameState state) {
        return registeredStates.contains(state);
    }
    
    /**
     * Obtém o estado inicial padrão
     */
    public GameState getInitialState() {
        return GameState.WAITING;
    }
    
    /**
     * Obtém o próximo estado automático
     */
    public GameState getNextState(GameState current) {
        if (current == null) return getInitialState();
        
        switch (current) {
            case WAITING:
                return GameState.STARTING;
                
            case STARTING:
                return GameState.INVINCIBILITY;
                
            case INVINCIBILITY:
                return GameState.PLAYING;
                
            case PLAYING:
                return GameState.FEAST; // Pode ser alterado dinamicamente
                
            case FEAST:
                return GameState.PLAYING; // Retorna ao jogo normal após feast
                
            case FINAL_BATTLE:
                return GameState.ENDING;
                
            case ENDING:
                return GameState.RESTARTING;
                
            case RESTARTING:
                return GameState.WAITING;
                
            default:
                return getInitialState();
        }
    }
    
    /**
     * Obtém a duração padrão de um estado em segundos
     */
    public long getStateDuration(GameState state) {
        switch (state) {
            case WAITING:
                return 0; // Indeterminado
            case STARTING:
                return 30; // 30 segundos
            case INVINCIBILITY:
                return 60; // 1 minuto
            case PLAYING:
                return 300; // 5 minutos (base, pode estender)
            case FEAST:
                return 120; // 2 minutos
            case FINAL_BATTLE:
                return 180; // 3 minutos
            case ENDING:
                return 15; // 15 segundos
            case RESTARTING:
                return 10; // 10 segundos
            default:
                return 0;
        }
    }
    
    /**
     * Verifica se um estado permite transições automáticas
     */
    public boolean allowsAutoTransition(GameState state) {
        return state != GameState.WAITING && state != GameState.RESTARTING;
    }
    
    /**
     * Obtém todos os estados registrados
     */
    public Set<GameState> getRegisteredStates() {
        return EnumSet.copyOf(registeredStates);
    }
}
