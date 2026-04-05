package com.minecraft.savagemc.hungergames.command;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;

/**
 * Comando de ajuda do HungerGames
 */
public class HelpCommand extends HungerGamesCommand {
    
    public HelpCommand(HungerGamesPlugin plugin) {
        super(plugin, "help", "Mostra a ajuda do HungerGames", "hungergames.command.help");
    }
    
    @Override
    protected boolean onCommand(org.bukkit.command.CommandSender sender, String[] args) {
        sendInfo(sender, "§e§lHungerGames - Ajuda");
        sender.sendMessage("");
        
        // Comandos básicos (todos podem usar)
        sendInfo(sender, "§7Comandos básicos:");
        sendCommandHelp(sender, "kit", "Seleciona um kit para jogar", true);
        sendCommandHelp(sender, "kits", "Lista todos os kits disponíveis", true);
        sendCommandHelp(sender, "stats", "Mostra suas estatísticas", true);
        sendCommandHelp(sender, "spectate", "Especta um jogo em andamento", true);
        
        sender.sendMessage("");
        
        // Comandos de jogo (se tiver permissão)
        if (sender.hasPermission("hungergames.command.start")) {
            sendInfo(sender, "§7Comandos de jogo:");
            sendCommandHelp(sender, "start", "Inicia uma partida", true);
            sendCommandHelp(sender, "stop", "Para a partida atual", true);
            sendCommandHelp(sender, "feast", "Gerencia o feast", false);
        }
        
        sender.sendMessage("");
        
        // Comandos administrativos (se tiver permissão)
        if (sender.hasPermission("hungergames.admin")) {
            sendInfo(sender, "§7Comandos administrativos:");
            sendCommandHelp(sender, "arena", "Gerencia as arenas", false);
            sendCommandHelp(sender, "forcekit", "Força um kit para um jogador", false);
            sendCommandHelp(sender, "hgadmin", "Painel administrativo", false);
        }
        
        sender.sendMessage("");
        sendInfo(sender, "§7Use §e/comando <ajuda> §7para ver ajuda de um comando específico.");
        sendInfo(sender, "§7Exemplo: §e/kit ajuda");
        
        return true;
    }
    
    /**
     * Envia ajuda de um comando específico
     */
    private void sendCommandHelp(org.bukkit.command.CommandSender sender, String command, String description, boolean hasPermission) {
        String permission = "hungergames.command." + command;
        boolean canUse = sender.hasPermission(permission);
        
        String color = canUse ? "§a" : "§c";
        String status = canUse ? "✓" : "✗";
        
        sender.sendMessage(" " + color + status + " §f/" + command + " §7- " + description + 
                         (hasPermission ? "" : " §8(Sem permissão)"));
    }
    
    @Override
    public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        return java.util.Arrays.asList();
    }
}
