package com.minecraft.arcade.eventos.settings;

import com.minecraft.arcade.eventos.managers.EventoTipo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Todos os valores que antes estavam em {@code config.yml}, fixos em código (sem arquivo de configuração).
 */
public final class EventEmbeddedConfig {

    private EventEmbeddedConfig() {
    }

    public static final String LOBBY_SERVER = "lobby";
    public static final String NOME_SERVIDOR_EVENTOS = "eventos";

    public static final int TEMPO_AREA_BOLO_SEGUNDOS = 15;
    public static final int CONTAGEM_REGRESSIVA_INICIAL = 10;
    public static final int TEMPO_PARA_INICIAR_SEGUNDOS = 300;
    public static final int MIN_JOGADORES_AUTO_INICIO = 5;
    public static final int RELOG_TIME_MINUTOS = 5;
    public static final int TEMPO_INVENCIBILIDADE_SEGUNDOS = 10;

    public static final String PASTA_SCHEMATICS = "schematics";
    public static final String SCHEMATIC_EVENTO = "mapa_evento";
    public static final String SCHEMATIC_BOLO = "area_bolo";

    public static final String SCOREBOARD_SITE = "§bspartamc.com.br";

    public static final int MAX_JOGADORES_EVENTO = 80;
    public static final int MAX_JOGADORES_POR_CLAN = 4;

    public static final boolean EVENTOS_CLANS = false;

    public static final EventoTipo EVENTO_TIPO_INICIAL = EventoTipo.SOLO;
    public static final String EVENTO_NOME_PADRAO = "Evento";
    public static final String EVENTO_RESENHA_NOME = "SPARTA";

    public static final boolean MUNDO_PLANO_ATIVO = true;
    public static final String MUNDO_PLANO_NOME = "world";

    public static final String SPAWN_EVENTO_WORLD = "world";
    public static final double SPAWN_EVENTO_X = 0;
    public static final double SPAWN_EVENTO_Y = 64;
    public static final double SPAWN_EVENTO_Z = 0;

    public static final String AREA_BOLO_WORLD = "world";
    public static final double AREA_BOLO_X = 0;
    public static final double AREA_BOLO_Y = 100;
    public static final double AREA_BOLO_Z = 0;
    public static final float AREA_BOLO_YAW = 0f;
    public static final float AREA_BOLO_PITCH = 0f;
    public static final int AREA_BOLO_RAIO_PLATAFORMA = 5;
    public static final boolean AREA_BOLO_GERAR_AUTOMATICO = true;
    public static final String AREA_BOLO_FORMATO = "cruz"; // cruz | retangulo
    public static final boolean AREA_BOLO_BASE_VIDRO = true;

    // Mensagens (configs antigas "mensagens.*" — use & nos defaults; código traduz onde preciso)
    public static final String MSG_EVENTO_INICIANDO = "&aO evento inicia em &e%tempo% &asegundos!";
    public static final String MSG_COUNTDOWN_INICIADO = "&aCountdown iniciado! Evento em &e%tempo% &asegundos.";
    public static final String MSG_EVENTO_INICIADO = "&aO evento começou! Boa sorte!";
    public static final String MSG_EVENTO_FINALIZADO = "&6O evento terminou! Vencedor: &e%vencedor%";
    public static final String MSG_TIME_GANHOU = "&aTIME Ganhou o jogo.";
    public static final String MSG_INDO_BOLO = "&eIndo para a área de celebração...";
    public static final String MSG_INDO_LOBBY = "&e.";
    public static final String MSG_SERVIDOR_REINICIANDO = "&cServidor reiniciando. Até já!";
    public static final String MSG_ESPECTADOR = "&7Você morreu! Modo espectador ativado.";
    public static final String MSG_RELOG_VOLTOU = "&aVocê voltou a tempo! Bem-vindo de volta.";
    public static final String MSG_TITULO_CONTAGEM = "&e%tempo%";
    public static final String MSG_JOIN_TITULO = "&e&lEVENTOS &8» &r&fbem-vindo";
    public static final String MSG_JOIN_SUBTITULO = "&7Sala de evento — &fboa sorte&7!";
    public static final String MSG_TITULO_CONTAGEM_SUB = "&7Prepare-se!";

    /**
     * Snapshot inicial das variáveis editáveis em runtime por {@code /var} (persistência só na RAM até reinício).
     */
    public static Map<String, String> newDefaultVariables() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("event.name", "EVENTO");
        m.put("event.chat", "true");
        m.put("event.pvp", "true");
        m.put("event.damage", "true");
        m.put("event.hunger", "true");
        m.put("event.blockplace", "false");
        m.put("event.min_players", String.valueOf(MIN_JOGADORES_AUTO_INICIO));
        m.put("event.max_players", String.valueOf(MAX_JOGADORES_EVENTO));
        m.put("event.tempo_iniciar", String.valueOf(TEMPO_PARA_INICIAR_SEGUNDOS));
        m.put("event.tempo_bolo", String.valueOf(TEMPO_AREA_BOLO_SEGUNDOS));
        m.put("event.contagem_inicial", String.valueOf(CONTAGEM_REGRESSIVA_INICIAL));
        m.put("event.invencibilidade", String.valueOf(TEMPO_INVENCIBILIDADE_SEGUNDOS));
        m.put("event.relog_minutos", String.valueOf(RELOG_TIME_MINUTOS));
        m.put("event.site", SCOREBOARD_SITE);
        m.put("event.lobby_server", LOBBY_SERVER);
        m.put("event.join_titulo", MSG_JOIN_TITULO);
        m.put("event.join_subtitulo", MSG_JOIN_SUBTITULO);
        m.put("event.msg_espectador", MSG_ESPECTADOR);
        m.put("event.msg_indo_bolo", MSG_INDO_BOLO);
        m.put("event.msg_relog_voltou", MSG_RELOG_VOLTOU);
        m.put("event.msg_evento_iniciando", MSG_EVENTO_INICIANDO);
        m.put("event.msg_countdown_iniciado", MSG_COUNTDOWN_INICIADO);
        m.put("event.msg_evento_iniciado", MSG_EVENTO_INICIADO);
        m.put("event.msg_time_ganhou", MSG_TIME_GANHOU);
        m.put("event.msg_evento_finalizado", MSG_EVENTO_FINALIZADO);
        m.put("event.eventos_clans", String.valueOf(EVENTOS_CLANS));
        m.put("event.max_por_clan", String.valueOf(MAX_JOGADORES_POR_CLAN));
        m.put("event.nome_resenha", EVENTO_RESENHA_NOME);
        m.put("event.auto_respawn", "true");
        m.put("event.titulo_contagem", MSG_TITULO_CONTAGEM);
        m.put("event.titulo_contagem_subtitulo", MSG_TITULO_CONTAGEM_SUB);
        m.put("event.nome_servidor_eventos", NOME_SERVIDOR_EVENTOS);
        m.put("event.persist_tipo", EVENTO_TIPO_INICIAL.name());
        m.put("event.kit", "none");
        m.put("event.freeze_contagem", "false");
        m.put("event.broadcast_kill", "false");
        m.put("event.firework_vitoria", "true");
        m.put("event.bolo_auto", String.valueOf(AREA_BOLO_GERAR_AUTOMATICO));
        m.put("event.cosmetics_chat", "true");
        m.put("event.cosmetics_nametag", "true");
        m.put("event.tag_contagem", "&7[&e*&7] &f");
        m.put("event.tag_vivo", "&7[&a*&7] &f");
        m.put("event.tag_espectador", "&7[&8::&7] &7");
        m.put("event.tag_vencedor", "&6[&l*&6] &f");
        m.put("event.medal_vitoria_icon", "&6*&r");
        m.put("event.medal_top_killer_icon", "&c||&r");
        m.put("event.medal_participacao_icon", "&7°&r");
        m.put("event.chat_corpo", "&f%1$s&8:&r &7%2$s");
        return m;
    }

    /** Cópia mutável das variáveis padrão. */
    public static HashMap<String, String> mutableDefaultVariablesCopy() {
        return new HashMap<>(newDefaultVariables());
    }

    public static Location resolveSpawnEvento(World fallbackWorld) {
        World w = Bukkit.getWorld(SPAWN_EVENTO_WORLD);
        if (w == null) w = fallbackWorld != null ? fallbackWorld : Bukkit.getWorlds().get(0);
        return new Location(w, SPAWN_EVENTO_X, SPAWN_EVENTO_Y, SPAWN_EVENTO_Z);
    }

    public static Location resolveAreaBolo(World fallbackWorld) {
        World w = Bukkit.getWorld(AREA_BOLO_WORLD);
        if (w == null) w = fallbackWorld != null ? fallbackWorld : Bukkit.getWorlds().get(0);
        return new Location(w, AREA_BOLO_X, AREA_BOLO_Y, AREA_BOLO_Z, AREA_BOLO_YAW, AREA_BOLO_PITCH);
    }
}
