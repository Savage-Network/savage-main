package com.minecraft.arcade.eventos.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EventoTabCompleter implements TabCompleter {

    private static final List<String> SUBS = Arrays.asList(
            "entrar", "join", "kills", "elim", "top", "ranking", "medalhas", "medals", "tag");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) return Collections.emptyList();
        String p = args[0].toLowerCase();
        return SUBS.stream()
                .filter(s -> s.startsWith(p))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
