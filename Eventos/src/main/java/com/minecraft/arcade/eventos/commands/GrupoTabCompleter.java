package com.minecraft.arcade.eventos.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GrupoTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            List<String> names = new ArrayList<>();
            names.add("sair");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p != sender) names.add(p.getName());
            }
            return names;
        }
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> names = new ArrayList<>();
            names.add("sair");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p != sender) names.add(p.getName());
            }
            return names.stream()
                    .filter(s -> s.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }
}
