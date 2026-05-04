package com.minecraft.arcade.eventos.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VarTabCompleter implements TabCompleter {

    private static final List<String> CHAVES = Arrays.asList(
            "event.name", "event.chat", "event.pvp", "event.damage", "event.hunger", "event.blockplace",
            "event.min_players", "event.max_players", "event.tempo_iniciar", "event.tempo_bolo", "event.contagem_inicial",
            "event.invencibilidade", "event.relog_minutos", "event.site",
            "event.lobby_server", "event.join_titulo", "event.join_subtitulo",
            "event.persist_tipo",
            "event.kit", "event.freeze_contagem", "event.broadcast_kill", "event.firework_vitoria", "event.bolo_auto",
            "event.msg_espectador", "event.msg_indo_bolo", "event.msg_relog_voltou", "event.msg_evento_iniciando", "event.msg_countdown_iniciado",
            "event.msg_evento_iniciado", "event.msg_time_ganhou", "event.msg_evento_finalizado",
            "event.eventos_clans", "event.max_por_clan", "event.nome_resenha",
            "event.auto_respawn", "event.titulo_contagem", "event.titulo_contagem_subtitulo", "event.nome_servidor_eventos",
            "event.cosmetics_chat", "event.cosmetics_nametag", "event.tag_contagem", "event.tag_vivo", "event.tag_espectador",
            "event.tag_vencedor", "event.medal_vitoria_icon", "event.medal_top_killer_icon", "event.medal_participacao_icon",
            "event.chat_corpo"
    );
    private static final List<String> VALORES_BOOL = Arrays.asList("true", "false");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("eventos.admin")) return java.util.Collections.emptyList();
        if (args.length == 0) return new ArrayList<>(CHAVES);
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return CHAVES.stream()
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            String key = args[0].toLowerCase();
            boolean boolKey = (key.contains("pvp") || key.contains("damage") || key.contains("hunger") || key.contains("blockplace") || key.contains("eventos_clans") || key.contains("auto_respawn")
                    || key.contains("freeze_contagem") || key.contains("broadcast_kill") || key.contains("firework_vitoria")
                    || key.contains("bolo_auto") || key.contains("cosmetics"))
                    || (key.contains("chat") && !key.equals("event.chat_corpo"));
            if (!boolKey) {
                return java.util.Collections.emptyList();
            }
            String partial = args[1].toLowerCase();
            return VALORES_BOOL.stream()
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }
}
