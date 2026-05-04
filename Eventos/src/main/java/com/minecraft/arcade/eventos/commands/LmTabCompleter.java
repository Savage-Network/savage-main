package com.minecraft.arcade.eventos.commands;

import com.minecraft.arcade.eventos.Eventos;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LmTabCompleter implements TabCompleter {

    private static final List<String> SUBS = Arrays.asList(
            "nome", "tipo", "parar", "status", "evento",
            "resenha", "padrao",
            "spleef", "folha", "sumo", "lavarun", "tntrun", "vidro"
    );
    private static final List<String> TIPOS = Arrays.asList("solo", "duplas", "trio", "panela");

    private final Eventos plugin;

    public LmTabCompleter(Eventos plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) return new ArrayList<>(SUBS);
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return SUBS.stream()
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList());
        }
        String sub = args[0].toLowerCase();
        String partial = args[args.length - 1].toLowerCase();
        if (sub.equals("tipo")) {
            return TIPOS.stream()
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList());
        }
        if (sub.equals("evento")) {
            List<String> opts = new ArrayList<>(Arrays.asList("list", "lista"));
            int n = plugin.getEventosPredefinidosManager().getQuantidade();
            for (int i = 1; i <= n; i++) opts.add(String.valueOf(i));
            if (partial.isEmpty()) return opts;
            return opts.stream()
                    .filter(s -> s.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }
}
