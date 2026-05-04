package com.minecraft.arcade.eventos.commands;

import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.managers.EventState;
import com.minecraft.arcade.eventos.settings.EventEmbeddedConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TempoCommand implements CommandExecutor {

    private final Eventos plugin;

    public TempoCommand(Eventos plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("eventos.admin")) {
            sender.sendMessage(ChatColor.RED + "Sem permissão.");
            return true;
        }
        if (plugin.getEventManager().getState() != EventState.AGUARDANDO) {
            sender.sendMessage(ChatColor.RED + "Só é possível usar /tempo quando o evento está aguardando.");
            return true;
        }
        if (args.length < 1) {
            int atual = plugin.getVariablesManager().getInt("event.tempo_iniciar", EventEmbeddedConfig.TEMPO_PARA_INICIAR_SEGUNDOS);
            sender.sendMessage(ChatColor.GOLD + "Tempo para iniciar: " + ChatColor.WHITE + atual + " segundos (" + (atual / 60) + " min)");
            sender.sendMessage(ChatColor.YELLOW + "Uso: /tempo <segundos> | /tempo start | /tempo parar");
            return true;
        }
        String arg = args[0].trim().toLowerCase();
        if (arg.equals("parar")) {
            plugin.getEventManager().pararCountdownParaIniciar();
            sender.sendMessage(ChatColor.GREEN + "Countdown parado.");
            return true;
        }
        if (arg.equals("start")) {
            int segundos = plugin.getVariablesManager().getInt("event.tempo_iniciar", EventEmbeddedConfig.TEMPO_PARA_INICIAR_SEGUNDOS);
            plugin.getEventManager().iniciarCountdownParaIniciar(segundos);
            sender.sendMessage(ChatColor.GREEN + "Countdown iniciado com " + segundos + " segundos.");
            return true;
        }
        int segundos;
        try {
            segundos = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Tempo inválido. Use um número de segundos (ex: 300 para 5 min).");
            return true;
        }
        if (segundos < 0) {
            sender.sendMessage(ChatColor.RED + "Use um número positivo de segundos.");
            return true;
        }
        plugin.getVariablesManager().set("event.tempo_iniciar", String.valueOf(segundos));
        plugin.getEventManager().iniciarCountdownParaIniciar(segundos);
        sender.sendMessage(ChatColor.GREEN + "Tempo definido para " + segundos + " segundos. Countdown iniciado.");
        return true;
    }
}
