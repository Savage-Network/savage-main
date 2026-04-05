package com.minecraft.savagemc.hungergames.game.state;

/**
 * Enumeração de estados possíveis do jogo HungerGames
 */
public enum GameState {
    
    /**
     * Aguardando jogadores entrarem
     */
    WAITING("Aguardando Jogadores", "§e§lAGUARDANDO"),
    
    /**
     * Contagem regressiva para iniciar
     */
    STARTING("Iniciando", "§6§lINICIANDO"),
    
    /**
     * Fase de invencibilidade inicial
     */
    INVINCIBILITY("Invencibilidade", "§a§lINVENCIBILIDADE"),
    
    /**
     * Jogo em andamento normal
     */
    PLAYING("Jogando", "§c§lJOGANDO"),
    
    /**
     * Feast aparecendo
     */
    FEAST("Feast", "§6§lFEAST"),
    
    /**
     * Batalha final
     */
    FINAL_BATTLE("Batalha Final", "§4§lBATALHA FINAL"),
    
    /**
     * Jogo terminando
     */
    ENDING("Terminando", "§d§lTERMINANDO"),
    
    /**
     * Reiniciando para próxima partida
     */
    RESTARTING("Reiniciando", "§8§lREINICIANDO");
    
    private final String displayName;
    private final String scoreboardTitle;
    
    GameState(String displayName, String scoreboardTitle) {
        this.displayName = displayName;
        this.scoreboardTitle = scoreboardTitle;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getScoreboardTitle() {
        return scoreboardTitle;
    }
    
    /**
     * Verifica se o estado permite que jogadores entrem
     */
    public boolean allowsJoin() {
        return this == WAITING || this == STARTING;
    }
    
    /**
     * Verifica se o estado permite que jogadores saiam
     */
    public boolean allowsLeave() {
        return this != PLAYING && this != FEAST && this != FINAL_BATTLE;
    }
    
    /**
     * Verifica se o jogo está em andamento
     */
    public boolean isPlaying() {
        return this == PLAYING || this == FEAST || this == FINAL_BATTLE;
    }
    
    /**
     * Verifica se o jogo está em fase de preparação
     */
    public boolean isPreGame() {
        return this == WAITING || this == STARTING || this == INVINCIBILITY;
    }
    
    /**
     * Verifica se o jogo está finalizando
     */
    public boolean isEnding() {
        return this == ENDING || this == RESTARTING;
    }
    
    /**
     * Verifica se o dano está desabilitado
     */
    public boolean isDamageDisabled() {
        return this == WAITING || this == STARTING || this == INVINCIBILITY || this == ENDING || this == RESTARTING;
    }
    
    /**
     * Verifica se o movimento está restrito
     */
    public boolean isMovementRestricted() {
        return this == STARTING || this == ENDING;
    }
    
    /**
     * Verifica se os kits podem ser usados
     */
    public boolean allowsKits() {
        return this == PLAYING || this == FEAST || this == FINAL_BATTLE;
    }
    
    /**
     * Verifica se o PVP está permitido
     */
    public boolean allowsPvP() {
        return this == PLAYING || this == FEAST || this == FINAL_BATTLE;
    }
    
    /**
     * Verifica se os blocos podem ser quebrados
     */
    public boolean allowsBlockBreaking() {
        return this == PLAYING || this == FEAST || this == FINAL_BATTLE;
    }
    
    /**
     * Verifica se os blocos podem ser colocados
     */
    public boolean allowsBlockPlacing() {
        return this == PLAYING || this == FEAST || this == FINAL_BATTLE;
    }
}
