package com.minecraft.savagemc.hungergames.listener;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import com.minecraft.savagemc.hungergames.game.state.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

/**
 * Listener para eventos de conexão de jogadores no HungerGames
 * Controla login, kick e pré-login
 */
public class ConnectionListener implements Listener {
    
    private final HungerGamesPlugin plugin;
    
    public ConnectionListener(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        // Verificar se o servidor está cheio
        if (plugin.getServer().getOnlinePlayers().size() >= plugin.getServer().getMaxPlayers()) {
            // Permitir VIPs entrarem mesmo com servidor cheio
            if (!isVIP(event.getUniqueId(), event.getName())) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, 
                    "§cServidor cheio!\n§7Compre VIP para entrar com servidor cheio.");
                return;
            }
        }
        
        // Verificar se o jogador está banido (usando sistema do SavageMC)
        if (isBanned(event.getUniqueId())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, 
                "§cVocê está banido deste servidor!");
            return;
        }
        
        // Permitir entrada
        event.allow();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        
        // Verificar se o login foi bem-sucedido no pré-login
        if (event.getResult() != Result.ALLOWED) {
            return;
        }
        
        // Verificar estado do jogo para decidir onde teleportar
        if (plugin.getGameManager().getCurrentGame() != null) {
            GameState currentState = plugin.getGameManager().getCurrentGame().getState();
            
            if (currentState.allowsJoin()) {
                // Jogador pode entrar no jogo
                plugin.getLogger().info("Player " + player.getName() + " can join the game");
            } else {
                // Jogador entrará como espectador ou aguardará
                plugin.getLogger().info("Player " + player.getName() + " will spectate or wait");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerKick(PlayerKickEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        
        // Registrar kick para estatísticas
        plugin.getLogger().info("Player " + player.getName() + " was kicked: " + event.getReason());
        
        // Limpar dados do jogador se necessário
        if (plugin.getUserManager().isUserOnline(player.getUniqueId())) {
            // Remover do jogo se estiver jogando
            if (plugin.getGameManager().getCurrentGame() != null) {
                if (plugin.getGameManager().getCurrentGame().hasPlayer(player.getUniqueId())) {
                    plugin.getGameManager().removePlayer(player.getUniqueId());
                }
                
                if (plugin.getGameManager().getCurrentGame().isSpectator(player.getUniqueId())) {
                    plugin.getGameManager().getCurrentGame().removeSpectator(player.getUniqueId());
                }
            }
            
            // Descarregar usuário
            plugin.getUserManager().unloadUser(player.getUniqueId());
        }
    }
    
    /**
     * Verifica se um jogador é VIP
     */
    private boolean isVIP(java.util.UUID playerUuid, String playerName) {
        // Usar sistema de contas do SavageMC
        com.minecraft.savagemc.core.account.Account account = plugin.getAccount(playerUuid);
        
        if (account == null) {
            // Tentar carregar pelo nome se não encontrar por UUID
            account = plugin.getAccount(playerName);
        }
        
        return account != null && account.isVIP();
    }
    
    /**
     * Verifica se um jogador está banido
     */
    private boolean isBanned(java.util.UUID playerUuid) {
        // Usar sistema de punições do SavageMC
        com.minecraft.savagemc.core.account.Account account = plugin.getAccount(playerUuid);
        
        if (account == null) {
            return false; // Se não encontrar conta, assumir que não está banido
        }
        
        // Verificar punições ativas
        com.minecraft.savagemc.core.api.punishment.Punishment activePunishment = 
            account.getActivePunishment(com.minecraft.savagemc.core.api.punishment.objects.enums.PunishmentCategory.BAN);
        
        return activePunishment != null && activePunishment.isValid();
    }
    
    /**
     * Verifica se o servidor está em modo manutenção
     */
    private boolean isMaintenanceMode() {
        return plugin.getConfigManager().getConfigBoolean("maintenance.enabled", false);
    }
    
    /**
     * Verifica se um jogador pode entrar em modo manutenção
     */
    private boolean canJoinMaintenance(java.util.UUID playerUuid) {
        com.minecraft.savagemc.core.account.Account account = plugin.getAccount(playerUuid);
        
        if (account == null) {
            return false;
        }
        
        // Permitir staff em modo manutenção
        return account.isStaffer();
    }
}
