package com.minecraft.savagemc.hungergames.command;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;

/**
 * Comando para ver estatísticas do HungerGames
 */
public class StatsCommand extends HungerGamesCommand {
    
    public StatsCommand(HungerGamesPlugin plugin) {
        super(plugin, "stats", "Mostra suas estatísticas do HungerGames", "hungergames.command.stats", true);
    }
    
    @Override
    protected boolean onCommand(org.bukkit.command.CommandSender sender, String[] args) {
        org.bukkit.entity.Player player = getPlayerSender(sender);
        if (player == null) {
            return false;
        }
        
        // Se tiver argumento, mostrar stats de outro jogador (se tiver permissão)
        if (hasArgs(args, 1)) {
            if (!sender.hasPermission("hungergames.admin.stats")) {
                sendError(sender, "Você não tem permissão para ver as estatísticas de outros jogadores.");
                return false;
            }
            
            String targetName = args[0];
            org.bukkit.entity.Player target = getOnlinePlayer(targetName);
            
            if (target == null) {
                sendError(sender, "Jogador não encontrado: " + targetName);
                return false;
            }
            
            showPlayerStats(sender, target);
            return true;
        }
        
        // Mostrar stats do próprio jogador
        showPlayerStats(sender, player);
        return true;
    }
    
    /**
     * Mostra as estatísticas de um jogador
     */
    private void showPlayerStats(org.bukkit.command.CommandSender sender, org.bukkit.entity.Player target) {
        com.minecraft.savagemc.hungergames.user.User user = plugin.getUserManager().getUser(target.getUniqueId());
        
        if (user == null) {
            sendError(sender, "Dados do jogador não encontrados.");
            return false;
        }
        
        boolean isSelf = sender.equals(target);
        String header = isSelf ? "§eSuas Estatísticas" : "§eEstatísticas de " + target.getName();
        
        sendInfo(sender, header);
        sender.sendMessage(" §7Partidas jogadas: §f" + user.getTotalGames());
        sender.sendMessage(" §7Vitórias: §f" + user.getTotalWins());
        sender.sendMessage(" §7Kills totais: §f" + user.getTotalKills());
        sender.sendMessage(" §7Mortes totais: §f" + user.getTotalDeaths());
        sender.sendMessage(" §7K/D Ratio: §f" + String.format("%.2f", user.getTotalKDRatio()));
        sender.sendMessage(" §7Taxa de vitória: §f" + String.format("%.1f%%", user.getWinRate() * 100));
        sender.sendMessage(" §7Tempo total jogado: §f" + user.getFormattedTotalPlayTime());
        
        // Se estiver em partida atual, mostrar stats da partida
        if (user.isPlaying()) {
            sender.sendMessage("");
            sendInfo(sender, "§ePartida Atual:");
            sender.sendMessage(" §7Kills na partida: §f" + user.getCurrentKills());
            sender.sendMessage(" §7Mortes na partida: §f" + user.getCurrentDeaths());
            sender.sendMessage(" §7K/D da partida: §f" + String.format("%.2f", user.getCurrentKDRatio()));
            
            long gameTime = System.currentTimeMillis() - user.getGameJoinTime();
            sender.sendMessage(" §7Tempo na partida: §f" + (gameTime / 1000) + " segundos");
        }
        
        // Mostrar kit atual
        com.minecraft.savagemc.hungergames.user.kits.Kit currentKit = plugin.getKitManager().getPlayerKit(target.getUniqueId());
        if (currentKit != null) {
            sender.sendMessage(" §7Kit atual: §f" + currentKit.getDisplayName());
        }
        
        return true;
    }
    
    @Override
    public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("hungergames.admin.stats")) {
            // Nomes de jogadores online
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(org.bukkit.entity.Player::getName)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        return java.util.Arrays.asList();
    }
}
