package com.minecraft.savagemc.hungergames.command;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;

/**
 * Comando para espectar jogos no HungerGames
 */
public class SpectateCommand extends HungerGamesCommand {
    
    public SpectateCommand(HungerGamesPlugin plugin) {
        super(plugin, "spectate", "Especta um jogo em andamento", "hungergames.command.spectate", true);
    }
    
    @Override
    protected boolean onCommand(org.bukkit.command.CommandSender sender, String[] args) {
        org.bukkit.entity.Player player = getPlayerSender(sender);
        if (player == null) {
            return false;
        }
        
        com.minecraft.savagemc.hungergames.game.instance.GameInstance currentGame = plugin.getGameManager().getCurrentGame();
        
        if (currentGame == null) {
            sendError(sender, "Não há nenhum jogo em andamento.");
            return false;
        }
        
        com.minecraft.savagemc.hungergames.game.state.GameState state = currentGame.getState();
        
        if (state == com.minecraft.savagemc.hungergames.game.state.GameState.WAITING || 
            state == com.minecraft.savagemc.hungergames.game.state.GameState.STARTING ||
            state == com.minecraft.savagemc.hungergames.game.state.GameState.RESTARTING) {
            
            sendError(sender, "Não é possível espectar neste momento.");
            return false;
        }
        
        // Verificar se o jogador já está espectando
        if (currentGame.isSpectator(player.getUniqueId())) {
            sendError(sender, "Você já está espectando este jogo.");
            return false;
        }
        
        // Verificar se o jogador está jogando
        if (currentGame.hasPlayer(player.getUniqueId())) {
            sendError(sender, "Você não pode espectar enquanto está jogando.");
            sendInfo(sender, "Use /leave para sair do jogo primeiro.");
            return false;
        }
        
        // Adicionar como espectador
        boolean success = currentGame.addSpectator(player.getUniqueId());
        
        if (success) {
            // Ativar modo espectador
            enableSpectatorMode(player);
            
            sendSuccess(sender, "Você agora está espectando o jogo!");
            sendInfo(sender, "Use /spectate novamente para parar de espectar.");
            
            // Atualizar estado do usuário
            com.minecraft.savagemc.hungergames.user.User user = plugin.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                user.setState(com.minecraft.savagemc.hungergames.user.UserState.SPECTATOR);
            }
        } else {
            sendError(sender, "Falha ao entrar no modo espectador.");
        }
        
        return true;
    }
    
    /**
     * Ativa o modo espectador para um jogador
     */
    private void enableSpectatorMode(org.bukkit.entity.Player player) {
        player.setGameMode(org.bukkit.GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);
        
        // Tornar invisível para outros jogadores
        for (org.bukkit.entity.Player other : plugin.getServer().getOnlinePlayers()) {
            other.hidePlayer(plugin, player);
            player.hidePlayer(plugin, other);
        }
        
        // Mostrar apenas espectadores
        for (org.bukkit.entity.Player other : plugin.getServer().getOnlinePlayers()) {
            if (plugin.getGameManager().getCurrentGame().isSpectator(other.getUniqueId())) {
                other.showPlayer(plugin, player);
                player.showPlayer(plugin, other);
            }
        }
    }
    
    /**
     * Desativa o modo espectador para um jogador
     */
    public static void disableSpectatorMode(HungerGamesPlugin plugin, org.bukkit.entity.Player player) {
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);
        
        // Tornar visível para todos
        for (org.bukkit.entity.Player other : plugin.getServer().getOnlinePlayers()) {
            other.showPlayer(plugin, player);
            player.showPlayer(plugin, other);
        }
    }
    
    @Override
    public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        return java.util.Arrays.asList();
    }
}
