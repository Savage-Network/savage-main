package com.minecraft.arcade.eventos.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TempoTabCompleter implements TabCompleter {

    private static final List<String> OPCOES = Arrays.asList("parar", "start", "60", "120", "180", "300", "600");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("eventos.admin")) return java.util.Collections.emptyList();
        if (args.length == 0) return new ArrayList<>(OPCOES);
        String partial = args[args.length - 1].toLowerCase();
        if (partial.isEmpty()) return new ArrayList<>(OPCOES);
        return OPCOES.stream()
                .filter(s -> s.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
    }
}
