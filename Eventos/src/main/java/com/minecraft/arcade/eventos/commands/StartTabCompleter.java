package com.minecraft.arcade.eventos.commands;

import com.minecraft.arcade.eventos.Eventos;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StartTabCompleter implements TabCompleter {

    private final Eventos plugin;

    public StartTabCompleter(Eventos plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("eventos.admin")) return java.util.Collections.emptyList();
        if (args.length != 1) return java.util.Collections.emptyList();
        String partial = args[0].toLowerCase();
        List<String> sugestoes = new ArrayList<>();
        sugestoes.add(plugin.getEventManager().getNomeEventoAtual());
        for (String id : plugin.getEventoRegistry().getAll().keySet()) {
            sugestoes.add(id);
        }
        for (com.minecraft.arcade.eventos.managers.EventosPredefinidosManager.EventoPredefinido ev : plugin.getEventosPredefinidosManager().getEventos()) {
            sugestoes.add(ev.getNome());
        }
        return sugestoes.stream()
                .filter(s -> s != null && s.toLowerCase().startsWith(partial))
                .distinct()
                .collect(Collectors.toList());
    }
}
