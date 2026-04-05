package com.minecraft.savagemc.hungergames.command;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;

/**
 * Comando para parar o jogo HungerGames
 */
public class StopCommand extends HungerGamesCommand {
    
    public StopCommand(HungerGamesPlugin plugin) {
        super(plugin, "stop", "Para a partida atual de HungerGames", "hungergames.command.stop", true);
    }
    
    @Override
    protected boolean onCommand(org.bukkit.command.CommandSender sender, String[] args) {
        org.bukkit.entity.Player player = getPlayerSender(sender);
        if (player == null) {
            return false;
        }
        
        // Verificar se existe um jogo em andamento
        if (plugin.getGameManager().getCurrentGame() == null) {
            sendError(sender, "Não há nenhum jogo em andamento.");
            return false;
        }
        
        com.minecraft.savagemc.hungergames.game.state.GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        if (currentState == com.minecraft.savagemc.hungergames.game.state.GameState.WAITING) {
            sendError(sender, "O jogo ainda não foi iniciado.");
            return false;
        }
        
        if (currentState == com.minecraft.savagemc.hungergames.game.state.GameState.RESTARTING) {
            sendError(sender, "O jogo já está finalizando.");
            return false;
        }
        
        // Finalizar o jogo
        plugin.getGameManager().endGame();
        
        sendSuccess(sender, "Jogo finalizado com sucesso!");
        
        // Notificar todos os jogadores
        plugin.getServer().broadcastMessage("§c§lO jogo HungerGames foi finalizado!");
        plugin.getServer().broadcastMessage("§7O jogo será reiniciado em breve...");
        
        return true;
    }
    
    @Override
    public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        return java.util.Arrays.asList();
    }
}
