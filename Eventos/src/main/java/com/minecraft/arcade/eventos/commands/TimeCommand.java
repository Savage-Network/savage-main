package com.minecraft.arcade.eventos.commands;

import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.managers.EventoTipo;
import com.minecraft.arcade.eventos.settings.EventEmbeddedConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TimeCommand implements CommandExecutor {

    private final Eventos plugin;

    public TimeCommand(Eventos plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("eventos.admin")) {
            sender.sendMessage(ChatColor.RED + "Sem permissão.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.GOLD + "Tipo atual: " + ChatColor.WHITE + plugin.getEventManager().getEventoTipo().name());
            sender.sendMessage(ChatColor.YELLOW + "Uso: /time <solo|duplas|trio|panela>");
            return true;
        }
        EventoTipo tipo = EventoTipo.parseTipo(args[0]);
        if (tipo == null) {
            sender.sendMessage(ChatColor.RED + "Tipo inválido. Use solo, duplas, trio ou panela.");
            return true;
        }
        plugin.getEventManager().setEventoTipo(tipo);
        plugin.getVariablesManager().set("event.persist_tipo", tipo.name());
        if (tipo.isEmTime()) {
            plugin.getVariablesManager().set("event.max_por_clan", String.valueOf(tipo.getTamanhoTime()));
        } else {
            plugin.getVariablesManager().set("event.max_por_clan", String.valueOf(EventEmbeddedConfig.MAX_JOGADORES_POR_CLAN));
        }
        sender.sendMessage(ChatColor.GREEN + "Tipo do evento definido para: " + ChatColor.WHITE + tipo.name()
                + (tipo.isEmTime() ? ChatColor.GRAY + " (times de " + tipo.getTamanhoTime() + ")" : ""));
        return true;
    }
}
