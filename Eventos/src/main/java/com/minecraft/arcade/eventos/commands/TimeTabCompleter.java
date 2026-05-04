package com.minecraft.arcade.eventos.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TimeTabCompleter implements TabCompleter {

    private static final List<String> TIPOS = Arrays.asList("solo", "duplas", "trio", "panela");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("eventos.admin")) return java.util.Collections.emptyList();
        if (args.length != 1) return java.util.Collections.emptyList();
        String partial = args[0].toLowerCase();
        if (partial.isEmpty()) return new ArrayList<>(TIPOS);
        return TIPOS.stream()
                .filter(s -> s.startsWith(partial))
                .collect(Collectors.toList());
    }
}
