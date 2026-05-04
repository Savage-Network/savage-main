package com.minecraft.arcade.eventos.managers;

import com.minecraft.arcade.eventos.Eventos;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Tags (prefijos de tab/nametag) e formatação de chat no evento + ícones de medalhas. Usa a mesma sidebar scoreboard no 1.8.
 */
public final class EventCosmeticsManager {

    private static final String TEAM_PREFIX = "evt";

    private final Eventos plugin;
    private final EventMedalStore medals;

    public EventCosmeticsManager(Eventos plugin, EventMedalStore medals) {
        this.plugin = plugin;
        this.medals = medals;
    }

    public EventMedalStore getMedalStore() {
        return medals;
    }

    /** Concede medalhas quando a rodada termina ({@link EventManager#finalizarEvento()}). */
    public void concederMedalhasFimRodada(Set<UUID> participantesSnap, UUID vencedorSolo,
            Set<UUID> vencedoresTimeSnap, Map<UUID, Integer> killsSnap) {
        if (participantesSnap == null || participantesSnap.isEmpty()) return;
        medals.addMedalTodos(participantesSnap, EventMedalStore.MEDAL_PARTICIPACAO);

        if (vencedoresTimeSnap != null && !vencedoresTimeSnap.isEmpty()) {
            medals.addMedalTodos(vencedoresTimeSnap, EventMedalStore.MEDAL_VITORIA);
        } else if (vencedorSolo != null && participantesSnap.contains(vencedorSolo)) {
            medals.addMedal(vencedorSolo, EventMedalStore.MEDAL_VITORIA);
        }

        if (killsSnap != null && !killsSnap.isEmpty()) {
            int max = killsSnap.values().stream().mapToInt(Integer::intValue).max().orElse(0);
            if (max >= 1) {
                List<UUID> tops = killsSnap.entrySet().stream()
                        .filter(e -> e.getValue() != null && e.getValue() == max)
                        .map(Map.Entry::getKey)
                        .filter(participantesSnap::contains)
                        .collect(Collectors.toList());
                for (UUID u : tops) {
                    medals.addMedal(u, EventMedalStore.MEDAL_TOP_KILLER);
                }
            }
        }
    }

    /** Ícones de medalhas acumuladas (com cores já traduzidas). */
    public String formatarListaMedalsParaMensagens(Player p) {
        Set<String> m = medals.getMedals(p.getUniqueId());
        if (m.isEmpty()) return "";
        String vm = plugin.getVariablesManager().get(
                "event.medal_" + EventMedalStore.MEDAL_VITORIA + "_icon", "&6*&r");
        String vk = plugin.getVariablesManager().get(
                "event.medal_" + EventMedalStore.MEDAL_TOP_KILLER + "_icon", "&c||&r");
        String vp = plugin.getVariablesManager().get(
                "event.medal_" + EventMedalStore.MEDAL_PARTICIPACAO + "_icon", "&7°&r");
        StringBuilder sb = new StringBuilder();
        if (m.contains(EventMedalStore.MEDAL_VITORIA)) {
            sb.append(ChatColor.translateAlternateColorCodes('&', vm));
        }
        if (m.contains(EventMedalStore.MEDAL_TOP_KILLER)) {
            sb.append(ChatColor.translateAlternateColorCodes('&', vk));
        }
        if (m.contains(EventMedalStore.MEDAL_PARTICIPACAO)) {
            sb.append(ChatColor.translateAlternateColorCodes('&', vp));
        }
        return sb.toString();
    }

    /** Texto bruto das tags (&), para edição por /var. */
    public String resolverTagRaw(Player p, EventManager em) {
        if (!plugin.getVariablesManager().getBoolean("event.cosmetics_nametag", true)
                && !plugin.getVariablesManager().getBoolean("event.cosmetics_chat", true)) {
            return "";
        }
        if (!em.isParticipando(p.getUniqueId())) return "";

        EventState st = em.getState();
        if (st == EventState.AGUARDANDO || st == EventState.REINICIANDO || st == EventState.ENVIANDO_LOBBY) {
            return "";
        }
        if (isVencedorExibirTag(em, st, p.getUniqueId())) {
            return plugin.getVariablesManager().get("event.tag_vencedor", "&6[&l*&6] &r");
        }
        if (em.isEspectador(p.getUniqueId())) {
            return plugin.getVariablesManager().get("event.tag_espectador", "&7[&8::&7] &7");
        }
        if (st == EventState.CONTAGEM) {
            return plugin.getVariablesManager().get("event.tag_contagem", "&7[&e*&7] &f");
        }
        return plugin.getVariablesManager().get("event.tag_vivo", "&7[&a*&7] &f");
    }

    /** Prefixo já com códigos § (nametag/chat). */
    public String resolverTag(Player p, EventManager em) {
        return ChatColor.translateAlternateColorCodes('&', resolverTagRaw(p, em));
    }

    private static boolean isVencedorExibirTag(EventManager em, EventState st, UUID uuid) {
        if (st != EventState.AREA_BOLO) return false;
        UUID solo = em.getUltimoVencedorSoloOpcional();
        if (solo != null && solo.equals(uuid)) return true;
        return em.isUuidNoTimeVencedor(uuid);
    }

    /** Atualiza prefix/suffix de tab/nametag se {@code event.cosmetics_nametag} ligado. */
    @SuppressWarnings("deprecation")
    public void refreshPlayer(Player player) {
        if (player == null || !player.isOnline()) return;
        EventManager em = plugin.getEventManager();
        Scoreboard sb = plugin.getEventScoreboard().scoreboardOuCria();
        if (!estadoParaCosmetics(em.getState()) || !em.isParticipando(player.getUniqueId())) {
            removerDeEquipasEvento(sb, player);
            return;
        }
        if (!plugin.getVariablesManager().getBoolean("event.cosmetics_nametag", true)) {
            removerDeEquipasEvento(sb, player);
            return;
        }

        String prefixo = resolverTag(player, em);
        String sufMedals = formatarListaMedalsParaMensagens(player);

        removerDeEquipasEvento(sb, player);

        Team team = obterOuCriarTeam(sb, nomeTeam(player.getUniqueId()));
        aplicarPrefSuf(team, prefixo != null ? prefixo : "", sufMedals != null ? sufMedals : "");
        team.addPlayer(player);
    }

    private static Team obterOuCriarTeam(Scoreboard sb, String nome) {
        Team t = sb.getTeam(nome);
        return t != null ? t : sb.registerNewTeam(nome);
    }

    private static String nomeTeam(UUID uuid) {
        String h = uuid.toString().replace("-", "");
        String tail = h.length() > 13 ? h.substring(0, 13) : h;
        return TEAM_PREFIX + tail;
    }

    @SuppressWarnings("deprecation")
    static void removerDeEquipasEvento(Scoreboard sb, Player player) {
        if (sb == null) return;
        for (Team t : new HashSet<>(sb.getTeams())) {
            if (t == null || !t.getName().startsWith(TEAM_PREFIX)) continue;
            if (t.hasPlayer(player)) {
                t.removePlayer(player);
            }
            try {
                if (t.getPlayers().isEmpty()) {
                    t.unregister();
                }
            } catch (Throwable ignored) {
            }
        }
    }

    public void removerTodasEquipasCosmeticDoScoreboardPublico() {
        Scoreboard sb = plugin.getEventScoreboard().scoreboardOuCria();
        for (Team t : new HashSet<>(sb.getTeams())) {
            if (t != null && t.getName().startsWith(TEAM_PREFIX)) {
                try {
                    t.unregister();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    public void refreshEmEventoTodos() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.getEventManager().isParticipando(p.getUniqueId())) {
                refreshPlayer(p);
            }
        }
    }

    public void onEventoAdministrativamenteParado() {
        removerTodasEquipasCosmeticDoScoreboardPublico();
    }

    private static boolean estadoParaCosmetics(EventState state) {
        return state == EventState.CONTAGEM || state == EventState.EM_JOGO || state == EventState.AREA_BOLO;
    }

    private static void aplicarPrefSuf(Team team, String prefix, String suffix) {
        team.setPrefix("");
        team.setSuffix("");
        String pfx = prefix == null ? "" : prefix;
        String suf = suffix == null ? "" : suffix;
        if (pfx.length() <= 16 && suf.length() <= 16) {
            team.setPrefix(pfx);
            team.setSuffix(suf);
            return;
        }
        if (pfx.length() <= 16) {
            team.setPrefix(pfx);
            aplicarSufixoCurto(team, suf);
            return;
        }
        aplicarLinha(team, pfx);
        aplicarSufixoCurto(team, suf);
    }

    private static void aplicarLinha(Team team, String texto) {
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
        String pfx = texto.substring(0, 16);
        String restante = texto.substring(16);
        String ultCor = ChatColor.getLastColors(pfx);
        int maxSuf = 16 - ultCor.length();
        if (maxSuf <= 0) {
            team.setPrefix(pfx);
            team.setSuffix("");
            return;
        }
        String s = restante.length() > maxSuf ? restante.substring(0, maxSuf) : restante;
        team.setPrefix(pfx);
        team.setSuffix(ultCor + s);
    }

    private static void aplicarSufixoCurto(Team team, String suffix) {
        if (suffix == null || suffix.isEmpty()) return;
        team.setSuffix(suffix.length() > 16 ? suffix.substring(0, 16) : suffix);
    }
}
