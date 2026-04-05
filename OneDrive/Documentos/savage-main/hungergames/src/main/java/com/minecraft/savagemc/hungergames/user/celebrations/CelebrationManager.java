package com.minecraft.savagemc.hungergames.user.celebrations;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerenciador de celebrações do HungerGames
 * Controla efeitos visuais e celebrações para vencedores
 */
@Getter
public class CelebrationManager {
    
    private final HungerGamesPlugin plugin;
    private final Map<UUID, Celebration> activeCelebrations;
    private final Map<String, Celebration> celebrationTypes;
    
    public CelebrationManager(HungerGamesPlugin plugin) {
        this.plugin = plugin;
        this.activeCelebrations = new ConcurrentHashMap<>();
        this.celebrationTypes = new ConcurrentHashMap<>();
    }
    
    /**
     * Inicializa o gerenciador de celebrações
     */
    public void initialize() {
        plugin.getLogger().info("Celebration Manager initialized");
        
        // Registrar tipos de celebração
        registerCelebrationTypes();
    }
    
    /**
     * Registra tipos de celebração
     */
    private void registerCelebrationTypes() {
        // Registrar celebração padrão
        registerCelebration("default", new DefaultCelebration(plugin));
        
        // Registrar celebração VIP
        registerCelebration("vip", new VipCelebration(plugin));
        
        // Registrar celebração especial
        registerCelebration("special", new SpecialCelebration(plugin));
    }
    
    /**
     * Registra um tipo de celebração
     */
    public void registerCelebration(String name, Celebration celebration) {
        celebrationTypes.put(name.toLowerCase(), celebration);
        plugin.getLogger().info("Registered celebration: " + name);
    }
    
    /**
     * Inicia uma celebração para um jogador
     */
    public boolean startCelebration(UUID playerUuid, String celebrationName) {
        org.bukkit.entity.Player player = plugin.getServer().getPlayer(playerUuid);
        
        if (player == null || !player.isOnline()) {
            return false;
        }
        
        // Verificar se já há uma celebração ativa
        if (activeCelebrations.containsKey(playerUuid)) {
            stopCelebration(playerUuid);
        }
        
        Celebration celebration = celebrationTypes.get(celebrationName.toLowerCase());
        
        if (celebration == null) {
            celebration = celebrationTypes.get("default"); // Usar padrão
        }
        
        if (celebration != null) {
            activeCelebrations.put(playerUuid, celebration);
            celebration.start(player);
            
            plugin.getLogger().info("Started celebration for player " + player.getName() + ": " + celebrationName);
            return true;
        }
        
        return false;
    }
    
    /**
     * Para uma celebração de um jogador
     */
    public boolean stopCelebration(UUID playerUuid) {
        Celebration celebration = activeCelebrations.remove(playerUuid);
        
        if (celebration != null) {
            celebration.stop();
            
            org.bukkit.entity.Player player = plugin.getServer().getPlayer(playerUuid);
            if (player != null && player.isOnline()) {
                plugin.getLogger().info("Stopped celebration for player: " + player.getName());
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Para todas as celebrações ativas
     */
    public void stopAllCelebrations() {
        for (Map.Entry<UUID, Celebration> entry : activeCelebrations.entrySet()) {
            UUID playerUuid = entry.getKey();
            Celebration celebration = entry.getValue();
            
            celebration.stop();
        }
        
        activeCelebrations.clear();
        plugin.getLogger().info("Stopped all active celebrations");
    }
    
    /**
     * Verifica se um jogador tem celebração ativa
     */
    public boolean hasActiveCelebration(UUID playerUuid) {
        return activeCelebrations.containsKey(playerUuid);
    }
    
    /**
     * Obtém a celebração ativa de um jogador
     */
    public Celebration getActiveCelebration(UUID playerUuid) {
        return activeCelebrations.get(playerUuid);
    }
    
    /**
     * Inicia celebração automática para vencedor
     */
    public void startWinnerCelebration(UUID playerUuid) {
        org.bukkit.entity.Player player = plugin.getServer().getPlayer(playerUuid);
        
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // Determinar tipo de celebração baseado no rank
        String celebrationType = determineCelebrationType(playerUuid);
        
        // Iniciar celebração
        startCelebration(playerUuid, celebrationType);
        
        // Anunciar vitória
        announceWinner(player);
    }
    
    /**
     * Determina o tipo de celebração baseado no rank
     */
    private String determineCelebrationType(UUID playerUuid) {
        com.minecraft.savagemc.core.account.Account account = plugin.getAccount(playerUuid);
        
        if (account == null) {
            return "default";
        }
        
        // VIPs e staff recebem celebração especial
        if (account.isVIP() || account.isStaffer()) {
            return "vip";
        }
        
        // Ranks mais altos recebem celebração especial
        if (account.hasRank(com.minecraft.savagemc.core.account.context.objects.rank.type.RankType.ELITE) ||
            account.hasRank(com.minecraft.savagemc.core.account.context.objects.rank.type.RankType.ULTIMATE) ||
            account.hasRank(com.minecraft.savagemc.core.account.context.objects.rank.type.RankType.MYTHIC)) {
            return "special";
        }
        
        return "default";
    }
    
    /**
     * Anuncia o vencedor para todos
     */
    private void announceWinner(org.bukkit.entity.Player winner) {
        // Mensagem de vitória no chat
        plugin.getServer().broadcastMessage("");
        plugin.getServer().broadcastMessage("§6§l╔════════════════════════════════════╗");
        plugin.getServer().broadcastMessage("§6§l║         §e§lVENCEDOR DO HUNGERGAMES!      §6§l║");
        plugin.getServer().broadcastMessage("§6§l║                                      §6§l║");
        plugin.getServer().broadcastMessage("§6§l║           §f§l" + winner.getName() + " §6§l           §6§l║");
        plugin.getServer().broadcastMessage("§6§l║                                      §6§l║");
        plugin.getServer().broadcastMessage("§6§l║           §e§lPARABÉNS! §6§l                   §6§l║");
        plugin.getServer().broadcastMessage("§6§l╚════════════════════════════════════╝");
        plugin.getServer().broadcastMessage("");
        
        // Title para todos
        for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.equals(winner)) {
                // Title especial para o vencedor
                player.sendTitle("§6§lVITÓRIA!", "§eParabéns por vencer!", 10, 70, 20);
            } else {
                // Title para outros jogadores
                player.sendTitle("§6§lFIM DE JOGO", "§c" + winner.getName() + " venceu!", 10, 70, 20);
            }
        }
        
        // Som de vitória
        winner.playSound(winner.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }
    
    /**
     * Limpa celebrações de jogadores desconectados
     */
    public void cleanupDisconnectedPlayers() {
        for (Map.Entry<UUID, Celebration> entry : activeCelebrations.entrySet()) {
            UUID playerUuid = entry.getKey();
            
            org.bukkit.entity.Player player = plugin.getServer().getPlayer(playerUuid);
            if (player == null || !player.isOnline()) {
                stopCelebration(playerUuid);
            }
        }
    }
    
    /**
     * Obtém estatísticas das celebrações
     */
    public String getStats() {
        return String.format(
            "CelebrationManager Stats - Active: %d, Types: %d",
            activeCelebrations.size(),
            celebrationTypes.size()
        );
    }
}
