package com.minecraft.arcade.eventos.commands;

import com.minecraft.arcade.eventos.Eventos;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EvtreloadCommand implements CommandExecutor {

    private final Eventos plugin;

    public EvtreloadCommand(Eventos plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("eventos.admin")) {
            sender.sendMessage(ChatColor.RED + "Sem permissão.");
            return true;
        }
        plugin.getVariablesManager().carregar();
        plugin.getEventosPredefinidosManager().carregar();
        plugin.aplicarVariaveisAoEstadoDoEvento();
        sender.sendMessage(ChatColor.GREEN + "Valores embutidos do Eventos foram recarregados (RAM); alterações ao vivo com /var foram perdidas.");
        return true;
    }
}
