package com.minecraft.savagemc.hungergames.user;

/**
 * Estados possíveis de um usuário no HungerGames
 */
public enum UserState {
    
    /**
     * Jogador está no lobby aguardando
     */
    LOBBY("No Lobby", "§a§lLOBBY"),
    
    /**
     * Jogador está em uma partida ativa
     */
    PLAYING("Jogando", "§c§lJOGANDO"),
    
    /**
     * Jogador está espectando uma partida
     */
    SPECTATOR("Espectador", "§7§lESPECTADOR"),
    
    /**
     * Jogador venceu a partida
     */
    WINNER("Vencedor", "§6§lVENCEDOR"),
    
    /**
     * Jogador está desconectado
     */
    OFFLINE("Offline", "§8§lOFFLINE");
    
    private final String displayName;
    private final String scoreboardPrefix;
    
    UserState(String displayName, String scoreboardPrefix) {
        this.displayName = displayName;
        this.scoreboardPrefix = scoreboardPrefix;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getScoreboardPrefix() {
        return scoreboardPrefix;
    }
    
    /**
     * Verifica se o estado permite interação com o jogo
     */
    public boolean canInteract() {
        return this == PLAYING;
    }
    
    /**
     * Verifica se o estado permite movimento
     */
    public boolean canMove() {
        return this != OFFLINE;
    }
    
    /**
     * Verifica se o estado permite receber dano
     */
    public boolean canTakeDamage() {
        return this == PLAYING;
    }
    
    /**
     * Verifica se o estado permite usar kits
     */
    public boolean canUseKits() {
        return this == PLAYING;
    }
    
    /**
     * Verifica se o estado permite PvP
     */
    public boolean canPvP() {
        return this == PLAYING;
    }
    
    /**
     * Verifica se o estado permite quebrar blocos
     */
    public boolean canBreakBlocks() {
        return this == PLAYING;
    }
    
    /**
     * Verifica se o estado permite colocar blocos
     */
    public boolean canPlaceBlocks() {
        return this == PLAYING;
    }
    
    /**
     * Verifica se o jogador está em uma partida
     */
    public boolean isInGame() {
        return this == PLAYING || this == SPECTATOR || this == WINNER;
    }
    
    /**
     * Verifica se o jogador está ativo
     */
    public boolean isActive() {
        return this != OFFLINE;
    }
}
