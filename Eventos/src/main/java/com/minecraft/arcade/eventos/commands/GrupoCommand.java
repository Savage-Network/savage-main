package com.minecraft.arcade.eventos.commands;

import com.minecraft.arcade.eventos.Eventos;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GrupoCommand implements CommandExecutor {

    private final Eventos plugin;

    public GrupoCommand(Eventos plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Apenas jogadores.");
            return true;
        }
        Player player = (Player) sender;
        if (!plugin.getClanEventManager().isEventosClansAtivos()) {
            player.sendMessage(ChatColor.RED + "Grupos desativados.");
            return true;
        }
        if (args.length < 1) {
            List<String> nomes = plugin.getClanEventManager().getNomesDoGrupo(player);
            player.sendMessage(ChatColor.GOLD + "Seu grupo: " + ChatColor.WHITE + String.join(", ", nomes));
            return true;
        }
        if (args[0].equalsIgnoreCase("sair")) {
            plugin.getClanEventManager().leaveGroup(player);
            player.sendMessage(ChatColor.YELLOW + "Você saiu do grupo.");
            return true;
        }
        String nick = args[0];
        Player alvo = Bukkit.getPlayerExact(nick);
        if (alvo == null || !alvo.isOnline()) {
            player.sendMessage(ChatColor.RED + "Jogador não encontrado: " + nick);
            return true;
        }
        if (alvo.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Você não pode adicionar a si mesmo.");
            return true;
        }
        if (plugin.getClanEventManager().addToGroup(player, alvo)) {
            player.sendMessage(ChatColor.GREEN + nick + " adicionado ao seu grupo.");
            alvo.sendMessage(ChatColor.GREEN + "Você entrou no grupo de " + player.getName() + ".");
        }
        return true;
    }
}
