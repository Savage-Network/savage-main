package com.minecraft.arcade.eventos.commands;

import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.managers.EventState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StartCommand implements CommandExecutor {

    private final Eventos plugin;

    public StartCommand(Eventos plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("eventos.admin")) {
            sender.sendMessage(ChatColor.RED + "Sem permissão.");
            return true;
        }

        if (plugin.getEventManager().getState() != EventState.AGUARDANDO) {
            sender.sendMessage(ChatColor.RED + "Já há um evento em andamento (" + plugin.getEventManager().getState().name() + "). Use /lm parar antes de iniciar outro.");
            return true;
        }

        String nomeEvento = plugin.getEventManager().getNomeEventoAtual();
        if (args.length > 0) {
            StringBuilder sb = new StringBuilder(args[0]);
            for (int i = 1; i < args.length; i++) sb.append(" ").append(args[i]);
            nomeEvento = sb.toString();
        }

        if (!plugin.getEventManager().iniciarEvento(nomeEvento)) {
            if (plugin.getEventManager().getJogadoresOnlineComPermissao() <= 0) {
                sender.sendMessage(ChatColor.RED + "Não há jogadores online com permissão eventos.jogar.");
            } else {
                sender.sendMessage(ChatColor.RED + "Não foi possível carregar o spawn do evento. Verifique os schematics/world e os logs do servidor.");
            }
            return true;
        }

        int n = plugin.getEventManager().getParticipantes().size();
        sender.sendMessage(ChatColor.GREEN + "Evento \"" + nomeEvento + "\" em contagem regressiva com " + ChatColor.WHITE + n + ChatColor.GREEN + " jogadores.");
        return true;
    }
}
