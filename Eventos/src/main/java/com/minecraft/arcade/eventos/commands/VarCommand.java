package com.minecraft.arcade.eventos.commands;

import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.managers.EventoTipo;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VarCommand implements CommandExecutor {

    private final Eventos plugin;

    public VarCommand(Eventos plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("eventos.admin")) {
            sender.sendMessage(ChatColor.RED + "Sem permissão.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "Uso: /var <chave> [valor]");
            sender.sendMessage(ChatColor.GRAY + "Ex: /var event.name " + ChatColor.WHITE + "Guerra dos Clans");
            sender.sendMessage(ChatColor.GRAY + "Variáveis: event.name, event.chat, event.pvp, event.damage, event.hunger, event.blockplace,");
            sender.sendMessage(ChatColor.GRAY + "  event.min_players, event.max_players, event.tempo_iniciar, event.tempo_bolo, event.contagem_inicial,");
            sender.sendMessage(ChatColor.GRAY + "  event.invencibilidade, event.relog_minutos, event.site, event.lobby_server, event.persist_tipo,");
            sender.sendMessage(ChatColor.GRAY + "  event.join_titulo, event.join_subtitulo, event.msg_*, event.eventos_clans, event.max_por_clan, event.nome_resenha,");
            sender.sendMessage(ChatColor.GRAY + "  event.auto_respawn, event.kit §7(none§8|§7pvp§8|§7knockback)§8, event.freeze_contagem, event.broadcast_kill, event.firework_vitoria, event.bolo_auto,");
            sender.sendMessage(ChatColor.GRAY + "  event.titulo_contagem, event.titulo_contagem_subtitulo, event.nome_servidor_eventos,");
            sender.sendMessage(ChatColor.GRAY + "  event.cosmetics_chat, event.cosmetics_nametag, event.tag_contagem/vivo/espectador/vencedor,");
            sender.sendMessage(ChatColor.GRAY + "  event.medal_vitoria_icon, event.medal_top_killer_icon, event.medal_participacao_icon, event.chat_corpo");
            return true;
        }
        String key = args[0].toLowerCase();
        if (args.length == 1) {
            String value = plugin.getVariablesManager().get(key);
            sender.sendMessage(ChatColor.GOLD + key + ChatColor.GRAY + " = " + ChatColor.WHITE + (value.isEmpty() ? "(vazio)" : value));
            return true;
        }
        StringBuilder sb = new StringBuilder(args[1]);
        for (int i = 2; i < args.length; i++) sb.append(" ").append(args[i]);
        String value = sb.toString();
        if ("event.persist_tipo".equals(key)) {
            if (EventoTipo.parseTipo(value) == null) {
                sender.sendMessage(ChatColor.RED + "Tipo inválido. Use solo, duplas, trio ou panela.");
                return true;
            }
        }
        plugin.getVariablesManager().set(key, value);
        if ("event.name".equals(key) || "event.persist_tipo".equals(key)) {
            plugin.aplicarVariavelAdministrativa(sender, key, value);
        }
        sender.sendMessage(ChatColor.GREEN + "Variável definida: " + ChatColor.WHITE + key + ChatColor.GRAY + " = " + ChatColor.WHITE + value);
        return true;
    }
}
