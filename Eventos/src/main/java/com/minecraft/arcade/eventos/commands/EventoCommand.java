package com.minecraft.arcade.eventos.commands;

import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.managers.EventCosmeticsManager;
import com.minecraft.arcade.eventos.managers.EventMedalStore;
import com.minecraft.arcade.eventos.managers.EventState;
import com.minecraft.arcade.eventos.utils.EventUiTheme;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * /evento entrar — entra na contagem regressiva (late join)<br>
 * /evento kills — suas eliminações na rodada atual<br>
 * /evento top — ranking por eliminações<br>
 * /evento medalhas — medalhas ganhas nos eventos (persistência em player-medals.yml)<br>
 * /evento tag — texto da tag atual (contagem, vivo, espectador ou vencedor na festa do bolo)
 */
public class EventoCommand implements CommandExecutor {

    private final Eventos plugin;

    public EventoCommand(Eventos plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Use no jogo, como jogador.");
            return true;
        }
        Player p = (Player) sender;

        if (args.length < 1) {
            ajuda(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "entrar":
            case "join":
                if (!p.hasPermission("eventos.jogar")) {
                    p.sendMessage(ChatColor.RED + "Sem permissão eventos.jogar.");
                    return true;
                }
                int ok = plugin.getEventManager().entrarNaContagem(p);
                if (ok == 0) {
                    p.sendMessage(EventUiTheme.CHAT_PREFIX + "§aEntrou no evento! Boa sorte.");
                } else if (ok == 1) {
                    p.sendMessage(ChatColor.RED + "Só dá para entrar durante a contagem (antes do GO).");
                } else if (ok == 2) {
                    p.sendMessage(ChatColor.YELLOW + "Você já está neste evento.");
                } else if (ok == 3) {
                    p.sendMessage(ChatColor.RED + "A sala já atingiu o máximo de jogadores (event.max_players).");
                } else if (ok == 5) {
                    p.sendMessage(ChatColor.RED + "Staff em vanish não entra no evento. Desative o vanish e use /evento entrar ou aguarde a sincronização na contagem.");
                } else {
                    p.sendMessage(ChatColor.RED + "Não foi possível teleportar até o spawn. Tente novamente.");
                }
                return true;

            case "kills":
            case "elim":
                if (!p.hasPermission("eventos.jogar")) {
                    p.sendMessage(ChatColor.RED + "Sem permissão.");
                    return true;
                }
                int k = plugin.getEventManager().getKills(p.getUniqueId());
                if (plugin.getEventManager().getState() != EventState.EM_JOGO || !plugin.getEventManager().isParticipando(p.getUniqueId())) {
                    p.sendMessage(ChatColor.GRAY + "Eliminações só contam durante o evento ativo como participante.");
                }
                p.sendMessage(ChatColor.GOLD + "Suas eliminações neste evento: " + ChatColor.WHITE + k);
                return true;

            case "top":
            case "ranking":
                if (plugin.getEventManager().getState() != EventState.EM_JOGO && plugin.getEventManager().getState() != EventState.CONTAGEM) {
                    p.sendMessage(ChatColor.GRAY + "Ranking disponível quando houver rodada em andamento ou na contagem (últimos valores zeram após vitória).");
                }
                List<Map.Entry<UUID, Integer>> list = ordenarRanking();
                if (list.isEmpty()) {
                    p.sendMessage(ChatColor.GRAY + "Ainda não há eliminações nesta rodada.");
                    return true;
                }
                p.sendMessage(ChatColor.GOLD + "★ Top eliminações");
                int pos = 1;
                for (Map.Entry<UUID, Integer> e : list) {
                    if (pos > 5) break;
                    String nome = Bukkit.getOfflinePlayer(e.getKey()).getName();
                    p.sendMessage(ChatColor.GRAY + " " + pos + ". " + ChatColor.WHITE + nome + ChatColor.GRAY + " — " + ChatColor.YELLOW + e.getValue());
                    pos++;
                }
                return true;

            case "medalhas":
            case "medals":
                if (!p.hasPermission("eventos.jogar")) {
                    p.sendMessage(ChatColor.RED + "Sem permissão.");
                    return true;
                }
                mostrarMedalhas(p);
                return true;

            case "tag":
                if (!p.hasPermission("eventos.jogar")) {
                    p.sendMessage(ChatColor.RED + "Sem permissão.");
                    return true;
                }
                EventCosmeticsManager cc = plugin.getEventCosmeticsManager();
                if (cc == null) {
                    p.sendMessage(ChatColor.GRAY + "Cosméticos indisponível.");
                    return true;
                }
                String raw = cc.resolverTagRaw(p, plugin.getEventManager());
                String vista = ChatColor.translateAlternateColorCodes('&', raw);
                p.sendMessage(EventUiTheme.CHAT_PREFIX + "§7Tag (config §8/& §8via §7/var): §r"
                        + (raw.isEmpty() ? "§o(vazio fora do momento com tag aplicável)§r" : raw));
                p.sendMessage(ChatColor.GRAY + "Preview colorido no chat/tab: §r" + vista);
                return true;

            default:
                ajuda(sender);
                return true;
        }
    }

    private List<Map.Entry<UUID, Integer>> ordenarRanking() {
        List<Map.Entry<UUID, Integer>> list = new ArrayList<>(plugin.getEventManager().getKillsCopiaParaRanking());
        list.sort(Map.Entry.<UUID, Integer>comparingByValue(Comparator.reverseOrder()));
        return list;
    }

    private void ajuda(CommandSender sender) {
        sender.sendMessage(EventUiTheme.CHAT_PREFIX + "§7/evento entrar §8— §fenfileirar na contagem");
        sender.sendMessage(EventUiTheme.CHAT_PREFIX + "§7/evento kills §8— §feliminações na rodada atual");
        sender.sendMessage(EventUiTheme.CHAT_PREFIX + "§7/evento top §8— §franking por eliminações");
        sender.sendMessage(EventUiTheme.CHAT_PREFIX + "§7/evento medalhas §8— §fmedalhas acumuladas (vitória, top kills, participação)");
        sender.sendMessage(EventUiTheme.CHAT_PREFIX + "§7/evento tag §8— §ftexto da tag no evento (preview)");
    }

    private void mostrarMedalhas(Player p) {
        java.util.Set<String> m = plugin.getEventMedalStore().getMedals(p.getUniqueId());
        if (m.isEmpty()) {
            p.sendMessage(EventUiTheme.CHAT_PREFIX + "§7Sem medalhas ainda. Ganhe ao jogar até ao fim (vitória §8·§7 top kills §8·§7 participação§7).");
            return;
        }
        StringBuilder lista = new StringBuilder();
        for (String id : m) {
            if (lista.length() > 0) lista.append("§8, ");
            lista.append(ChatColor.WHITE).append(nomeAmigávelMedalha(id));
        }
        p.sendMessage(EventUiTheme.CHAT_PREFIX + "§7Medalhas: " + lista);
    }

    private static String nomeAmigávelMedalha(String id) {
        switch (id) {
            case EventMedalStore.MEDAL_VITORIA:
                return "Vitória";
            case EventMedalStore.MEDAL_TOP_KILLER:
                return "Top eliminações";
            case EventMedalStore.MEDAL_PARTICIPACAO:
                return "Participação";
            default:
                return id;
        }
    }
}
