package com.minecraft.savagemc.hungergames.listener;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import com.minecraft.savagemc.hungergames.game.state.GameState;
import com.minecraft.savagemc.hungergames.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

/**
 * Listener para eventos de jogadores no HungerGames
 * Controla entrada, saída e interações dos jogadores
 */
public class PlayerListener implements Listener {
    
    private final HungerGamesPlugin plugin;
    
    public PlayerListener(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        
        // Carregar usuário
        User user = plugin.getUserManager().loadUser(player.getUniqueId(), player.getName());
        
        // Mensagem de join personalizada
        event.setJoinMessage(null); // Remover mensagem padrão
        
        // Verificar estado do jogo
        if (plugin.getGameManager().getCurrentGame() != null) {
            GameState currentState = plugin.getGameManager().getCurrentGame().getState();
            
            if (currentState.allowsJoin()) {
                // Tentar adicionar ao jogo
                boolean added = plugin.getGameManager().addPlayer(player.getUniqueId());
                
                if (added) {
                    player.sendMessage("§aVocê entrou no HungerGames!");
                    player.sendMessage("§7Selecione seu kit com §e/kit");
                    
                    // Teleportar para lobby
                    teleportToLobby(player);
                } else {
                    player.sendMessage("§cNão foi possível entrar no jogo.");
                    teleportToLobby(player);
                }
            } else {
                // Jogo já começou, entrar como espectador
                player.sendMessage("§eO jogo já está em andamento.");
                player.sendMessage("§7Você pode espectar com §e/spectate");
                
                teleportToLobby(player);
            }
        } else {
            // Nenhum jogo ativo
            player.sendMessage("§eBem-vindo ao HungerGames!");
            player.sendMessage("§7Aguarde o próximo jogo começar.");
            
            teleportToLobby(player);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        
        // Remover mensagem padrão
        event.setQuitMessage(null);
        
        // Remover do jogo se estiver jogando
        if (plugin.getGameManager().getCurrentGame() != null) {
            if (plugin.getGameManager().getCurrentGame().hasPlayer(player.getUniqueId())) {
                plugin.getGameManager().removePlayer(player.getUniqueId());
                
                // Notificar outros jogadores
                plugin.getServer().broadcastMessage("§c" + player.getName() + " saiu do jogo!");
            }
            
            if (plugin.getGameManager().getCurrentGame().isSpectator(player.getUniqueId())) {
                plugin.getGameManager().getCurrentGame().removeSpectator(player.getUniqueId());
            }
        }
        
        // Descarregar usuário
        plugin.getUserManager().unloadUser(player.getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        org.bukkit.entity.Player player = event.getEntity();
        
        // Verificar se está em jogo
        if (plugin.getGameManager().getCurrentGame() == null || 
            !plugin.getGameManager().getCurrentGame().hasPlayer(player.getUniqueId())) {
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        if (!currentState.isPlaying()) {
            return;
        }
        
        // Limpar mensagem de morte padrão
        event.setDeathMessage(null);
        
        // Remover drops de morte
        event.getDrops().clear();
        
        // Atualizar estatísticas
        User user = plugin.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            user.addDeath();
            user.endCurrentGame(false);
        }
        
        // Remover do jogo
        plugin.getGameManager().removePlayer(player.getUniqueId());
        
        // Adicionar como espectador
        plugin.getGameManager().getCurrentGame().addSpectator(player.getUniqueId());
        
        // Ativar modo espectador
        SpectateCommand.disableSpectatorMode(plugin, player);
        player.setGameMode(org.bukkit.GameMode.SPECTATOR);
        
        // Enviar mensagens
        player.sendMessage("§cVocê morreu!");
        player.sendMessage("§7Agora você está espectando. Use §e/spectate §7para sair.");
        
        // Notificar sobre o assassino se houver
        if (player.getKiller() != null) {
            org.bukkit.entity.Player killer = player.getKiller();
            plugin.getServer().broadcastMessage("§c" + player.getName() + " foi eliminado por " + killer.getName() + "!");
            
            // Atualizar estatísticas do assassino
            User killerUser = plugin.getUserManager().getUser(killer.getUniqueId());
            if (killerUser != null) {
                killerUser.addKill();
            }
        } else {
            plugin.getServer().broadcastMessage("§c" + player.getName() + " foi eliminado!");
        }
        
        // Verificar se o jogo acabou
        checkGameEnd();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        
        // Se estiver espectando, teleportar para localização adequada
        if (plugin.getGameManager().getCurrentGame() != null && 
            plugin.getGameManager().getCurrentGame().isSpectator(player.getUniqueId())) {
            
            // Teleportar para o centro da arena
            com.minecraft.savagemc.hungergames.arena.Arena arena = plugin.getArenaManager().getCurrentArena();
            if (arena != null) {
                org.bukkit.Location center = arena.getCenterLocation();
                if (center != null) {
                    event.setRespawnLocation(center);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();
        
        // Verificar se está em jogo e bloquear comandos não permitidos
        if (plugin.getGameManager().getCurrentGame() != null && 
            plugin.getGameManager().getCurrentGame().hasPlayer(player.getUniqueId())) {
            
            GameState currentState = plugin.getGameManager().getCurrentGame().getState();
            
            // Comandos permitidos durante o jogo
            if (command.startsWith("/hg") || command.startsWith("/hungergames") ||
                command.startsWith("/kit") || command.startsWith("/kits") ||
                command.startsWith("/stats") || command.startsWith("/spectate") ||
                command.startsWith("/leave")) {
                return;
            }
            
            // Permitir comandos de admin
            if (player.hasPermission("hungergames.admin")) {
                return;
            }
            
            // Bloquear outros comandos durante o jogo
            if (currentState.isPlaying()) {
                event.setCancelled(true);
                player.sendMessage("§cVocê não pode usar este comando durante o jogo!");
            }
        }
    }
    
    /**
     * Teleporta um jogador para o lobby
     */
    private void teleportToLobby(org.bukkit.entity.Player player) {
        // Teleportar para spawn do mundo ou localização configurada
        org.bukkit.World world = plugin.getServer().getWorlds().get(0);
        if (world != null) {
            player.teleport(world.getSpawnLocation());
        }
    }
    
    /**
     * Verifica se o jogo deve terminar
     */
    private void checkGameEnd() {
        if (plugin.getGameManager().getCurrentGame() == null) {
            return;
        }
        
        int alivePlayers = plugin.getGameManager().getCurrentGame().getPlayerCount();
        
        if (alivePlayers <= 1) {
            // Terminar o jogo
            plugin.getGameManager().endGame();
            
            if (alivePlayers == 1) {
                // Anunciar vencedor
                for (java.util.UUID playerUuid : plugin.getGameManager().getCurrentGame().getPlayers()) {
                    org.bukkit.entity.Player player = plugin.getServer().getPlayer(playerUuid);
                    if (player != null && player.isOnline()) {
                        plugin.getServer().broadcastMessage("§6§l" + player.getName() + " venceu o HungerGames!");
                        
                        // Atualizar estatísticas do vencedor
                        User user = plugin.getUserManager().getUser(playerUuid);
                        if (user != null) {
                            user.endCurrentGame(true);
                        }
                        break;
                    }
                }
            } else {
                plugin.getServer().broadcastMessage("§c§lNão há vencedores! O jogo terminou.");
            }
        }
    }
}
