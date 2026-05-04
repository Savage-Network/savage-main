package com.minecraft.arcade.eventos.scoreboard;

import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.managers.EventCosmeticsManager;
import com.minecraft.arcade.eventos.managers.EventState;
import com.minecraft.arcade.eventos.managers.EventoTipo;
import com.minecraft.arcade.eventos.settings.EventEmbeddedConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Scoreboard alinhada ao estilo Savage (§7 rótulos §8: §f valores, título em destaque).
 */
public class EventScoreboard {

    private static final String TITLE_PREFIX = "§e§l";
    private static final int TITLE_MAX_LENGTH = 32;
    private static final String ENTRY_JOG = "§a§r";
    private static final String ENTRY_TIPO = "§b§r";
    private static final String ENTRY_SITE = "§c§r";
    private static final String ENTRY_SP1 = "§d§r";
    private static final String ENTRY_SP2 = "§e§r";
    private static final String ENTRY_SP3 = "§f§r";
    private static final String ENTRY_TEMPO = "§0§r";

    private final Eventos plugin;
    private Scoreboard scoreboard;
    private Objective objective;
    private Team teamJogadores;
    private Team teamTipo;
    private Team teamSite;
    private Team teamSpace1;
    private Team teamSpace2;
    private Team teamSpace3;
    private Team teamTempo;

    public EventScoreboard(Eventos plugin) {
        this.plugin = plugin;
    }

    private void criar() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("evento", "dummy");
        objective.setDisplayName(getTituloFormatado());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        teamTempo = scoreboard.registerNewTeam("tempo");
        teamTempo.addEntry(ENTRY_TEMPO);
        teamTempo.setPrefix("");
        objective.getScore(ENTRY_TEMPO).setScore(7);

        teamSpace1 = scoreboard.registerNewTeam("sp1");
        teamSpace1.addEntry(ENTRY_SP1);
        teamSpace1.setPrefix(" ");
        objective.getScore(ENTRY_SP1).setScore(6);

        teamJogadores = scoreboard.registerNewTeam("jog");
        teamJogadores.addEntry(ENTRY_JOG);
        objective.getScore(ENTRY_JOG).setScore(5);

        teamSpace2 = scoreboard.registerNewTeam("sp2");
        teamSpace2.addEntry(ENTRY_SP2);
        teamSpace2.setPrefix(" ");
        objective.getScore(ENTRY_SP2).setScore(4);

        teamTipo = scoreboard.registerNewTeam("tipo");
        teamTipo.addEntry(ENTRY_TIPO);
        objective.getScore(ENTRY_TIPO).setScore(3);

        teamSpace3 = scoreboard.registerNewTeam("sp3");
        teamSpace3.addEntry(ENTRY_SP3);
        teamSpace3.setPrefix(" ");
        objective.getScore(ENTRY_SP3).setScore(2);

        teamSite = scoreboard.registerNewTeam("site");
        teamSite.addEntry(ENTRY_SITE);
        String site = plugin.getVariablesManager().get("event.site", EventEmbeddedConfig.SCOREBOARD_SITE);
        teamSite.setPrefix(site.length() > 16 ? site.substring(0, 16) : site);
        objective.getScore(ENTRY_SITE).setScore(1);
    }

    /** Título do topo da scoreboard (variável event.name + §b§l). Limite 32 chars no 1.8. */
    private String getTituloFormatado() {
        String nome = plugin.getVariablesManager().get("event.name", "EVENTO");
        if (nome.isEmpty()) nome = "EVENTO";
        String titulo = TITLE_PREFIX + nome;
        return titulo.length() > TITLE_MAX_LENGTH ? titulo.substring(0, TITLE_MAX_LENGTH) : titulo;
    }

    private String tipoParaTexto(EventoTipo tipo) {
        if (tipo == null) return "Solo";
        switch (tipo) {
            case PANELA: return "Panela";
            case DUPLAS: return "Dupla";
            case TRIO: return "Trio";
            default: return "Solo";
        }
    }

    /** Formata segundos totais como MM:SS (ex.: 300 -> "05:00", 65 -> "01:05"). */
    private String formatarTempo(int totalSegundos) {
        if (totalSegundos < 0) totalSegundos = 0;
        int min = totalSegundos / 60;
        int seg = totalSegundos % 60;
        return String.format("%02d:%02d", min, seg);
    }

    /** Sidebar usada também para equipas de prefixo (tags) no 1.8. */
    public Scoreboard scoreboardOuCria() {
        if (scoreboard == null) {
            criar();
        }
        return scoreboard;
    }

    public void atualizar() {
        if (scoreboard == null) criar();

        objective.setDisplayName(getTituloFormatado());

        int tempoIniciar = plugin.getEventManager().getTempoRestanteParaIniciar();
        int tempoContagem = plugin.getEventManager().getTempoRestanteContagemRegressiva();
        String linhaTempo;
        if (tempoContagem >= 0) {
            linhaTempo = "§7Tempo§8: §f" + formatarTempo(tempoContagem);
        } else if (tempoIniciar >= 0) {
            linhaTempo = "§7Tempo§8: §f" + formatarTempo(tempoIniciar);
        } else if (plugin.getEventManager().getState() == EventState.AGUARDANDO) {
            int tempoSegundos = plugin.getVariablesManager().getInt("event.tempo_iniciar", EventEmbeddedConfig.TEMPO_PARA_INICIAR_SEGUNDOS);
            linhaTempo = "§7Tempo§8: §f" + formatarTempo(tempoSegundos);
        } else {
            linhaTempo = "";
        }
        aplicarLinha(teamTempo, linhaTempo);

        int online = Bukkit.getOnlinePlayers().size();
        int max = plugin.getVariablesManager().getInt("event.max_players", EventEmbeddedConfig.MAX_JOGADORES_EVENTO);
        aplicarLinha(teamJogadores, "§7Jogadores§8: §f" + online + "§8/§f" + max);

        if (plugin.getEventManager().getState() == EventState.EM_JOGO) {
            aplicarLinha(teamTipo, plugin.getEventManager().getLinhaRankingKillsScoreboard());
        } else {
            aplicarLinha(teamTipo, "§7Tipo§8: §a" + tipoParaTexto(plugin.getEventManager().getEventoTipo()));
        }

        String site = plugin.getVariablesManager().get("event.site", EventEmbeddedConfig.SCOREBOARD_SITE);
        int siteLen = site.length();
        teamSite.setPrefix(siteLen > 16 ? site.substring(0, 16) : site);
        teamSite.setSuffix(siteLen > 16 ? site.substring(16) : "");
    }

    /** Aplica texto à team respeitando limite de 16 chars por prefix/suffix no 1.8. */
    private void aplicarLinha(Team team, String texto) {
        if (texto == null || texto.isEmpty()) {
            team.setPrefix("");
            team.setSuffix("");
            return;
        }
        if (texto.length() <= 16) {
            team.setPrefix(texto);
            team.setSuffix("");
            return;
        }
        String prefix = texto.substring(0, 16);
        String restante = texto.substring(16);
        String ultimaCor = ChatColor.getLastColors(prefix);
        int maxSuf = 16 - ultimaCor.length();
        if (maxSuf <= 0) {
            team.setPrefix(prefix);
            team.setSuffix("");
            return;
        }
        String suf = restante.length() > maxSuf ? restante.substring(0, maxSuf) : restante;
        team.setPrefix(prefix);
        team.setSuffix(ultimaCor + suf);
    }

    public void mostrarPara(Player player) {
        if (scoreboard == null) criar();
        atualizar();
        player.setScoreboard(scoreboard);
        EventCosmeticsManager cos = plugin.getEventCosmeticsManager();
        if (cos != null) {
            cos.refreshPlayer(player);
        }
    }

    public void mostrarParaTodos() {
        atualizar();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("eventos.jogar")) {
                p.setScoreboard(scoreboard);
            }
        }
    }

    public void remover(Player player) {
        if (player != null && player.isOnline()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }
}
