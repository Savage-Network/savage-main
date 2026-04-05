package com.minecraft.savagemc.hungergames.game.instance;

import lombok.Getter;

import java.util.UUID;

/**
 * Representa um jogador dentro de uma instância de jogo
 * Contém informações específicas da partida atual
 */
@Getter
public class GamePlayer {
    
    private final UUID playerUuid;
    private long joinTime;
    private int kills;
    private int deaths;
    private boolean alive;
    private boolean eliminated;
    private long eliminationTime;
    private UUID eliminatedBy;
    
    public GamePlayer(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.joinTime = System.currentTimeMillis();
        this.kills = 0;
        this.deaths = 0;
        this.alive = true;
        this.eliminated = false;
        this.eliminationTime = 0;
        this.eliminatedBy = null;
    }
    
    /**
     * Incrementa o contador de kills
     */
    public void addKill() {
        kills++;
    }
    
    /**
     * Incrementa o contador de deaths
     */
    public void addDeath() {
        deaths++;
    }
    
    /**
     * Elimina o jogador
     */
    public void eliminate(UUID eliminatedBy) {
        this.alive = false;
        this.eliminated = true;
        this.eliminationTime = System.currentTimeMillis();
        this.eliminatedBy = eliminatedBy;
        addDeath();
    }
    
    /**
     * Ressuscita o jogador (usado em casos especiais)
     */
    public void revive() {
        this.alive = true;
        this.eliminated = false;
        this.eliminationTime = 0;
        this.eliminatedBy = null;
    }
    
    /**
     * Verifica se o jogador está vivo
     */
    public boolean isAlive() {
        return alive && !eliminated;
    }
    
    /**
     * Verifica se o jogador foi eliminado
     */
    public boolean isEliminated() {
        return eliminated;
    }
    
    /**
     * Obtém o tempo que o jogador está na partida
     */
    public long getTimeInGame() {
        return System.currentTimeMillis() - joinTime;
    }
    
    /**
     * Obtém o K/D ratio
     */
    public double getKDRatio() {
        return deaths == 0 ? kills : (double) kills / deaths;
    }
    
    /**
     * Reseta as estatísticas do jogador
     */
    public void resetStats() {
        this.kills = 0;
        this.deaths = 0;
        this.alive = true;
        this.eliminated = false;
        this.eliminationTime = 0;
        this.eliminatedBy = null;
    }
}
