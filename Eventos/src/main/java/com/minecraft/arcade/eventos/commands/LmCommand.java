package com.minecraft.arcade.eventos.commands;

import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.eventos.EventoPronto;
import com.minecraft.arcade.eventos.managers.EventosPredefinidosManager;
import com.minecraft.arcade.eventos.managers.EventState;
import com.minecraft.arcade.eventos.managers.EventoTipo;
import com.minecraft.arcade.eventos.settings.EventEmbeddedConfig;
import com.minecraft.arcade.eventos.utils.EventUiTheme;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * /lm - Define o evento (nome, tipo, parar, status). Não inicia; use /start para iniciar.
 */
public class LmCommand implements CommandExecutor {

    private final Eventos plugin;

    public LmCommand(Eventos plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            enviarAjuda(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        // Evento resenha/padrão: sem mapa especial, usa schematic no /start
        if (sub.equals("resenha") || sub.equals("padrao")) {
            if (!sender.hasPermission("eventos.admin")) {
                sender.sendMessage(ChatColor.RED + "Sem permissão.");
                return true;
            }
            plugin.getEventManager().limparArenaAtual();
            plugin.getEventManager().setEventoProntoAtual(null);
            String nomeResenha = plugin.getVariablesManager().get("event.nome_resenha", EventEmbeddedConfig.EVENTO_RESENHA_NOME);
            plugin.getEventManager().setNomeEventoAtual(nomeResenha);
            plugin.getEventManager().setEventoTipo(EventoTipo.SOLO);
            plugin.getVariablesManager().set("event.name", nomeResenha);
            plugin.getVariablesManager().set("event.persist_tipo", EventoTipo.SOLO.name());
            plugin.getVariablesManager().set("event.max_por_clan", String.valueOf(EventEmbeddedConfig.MAX_JOGADORES_POR_CLAN));
            sender.sendMessage(ChatColor.GREEN + "Evento padrão selecionado: " + ChatColor.WHITE + nomeResenha
                    + ChatColor.GRAY + " (sem mapa especial; usa schematic no /start)");
            sender.sendMessage(ChatColor.GRAY + "Use /start para iniciar.");
            return true;
        }

        // Eventos prontos com geração de mapa: /lm spleef, /lm folha, /lm sumo, /lm lavarun, /lm tntrun
        EventoPronto eventoPronto = plugin.getEventoRegistry().get(sub);
        if (eventoPronto != null) {
            if (!sender.hasPermission("eventos.admin")) {
                sender.sendMessage(ChatColor.RED + "Sem permissão.");
                return true;
            }
            plugin.getEventManager().limparArenaAtual();
            plugin.getEventManager().setEventoProntoAtual(eventoPronto);
            plugin.getEventManager().setNomeEventoAtual(eventoPronto.getNome());
            plugin.getEventManager().setEventoTipo(eventoPronto.getTipo());
            plugin.getVariablesManager().set("event.name", eventoPronto.getNome());
            plugin.getVariablesManager().set("event.persist_tipo", eventoPronto.getTipo().name());
            if (eventoPronto.getTipo().isEmTime()) {
                plugin.getVariablesManager().set("event.max_por_clan", String.valueOf(eventoPronto.getTipo().getTamanhoTime()));
            } else {
                plugin.getVariablesManager().set("event.max_por_clan", String.valueOf(EventEmbeddedConfig.MAX_JOGADORES_POR_CLAN));
            }
            org.bukkit.Location spawn = plugin.getEventManager().prepararArenaAgora();
            if (spawn != null) {
                org.bukkit.Location meio = spawn.clone().add(0, 1, 0);
                org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                        p.teleport(meio);
                    }
                }, 5L);
            }
            sender.sendMessage(ChatColor.GREEN + "Evento selecionado: " + ChatColor.WHITE + eventoPronto.getNome()
                    + ChatColor.GRAY + " (arena carregada)");
            sender.sendMessage(ChatColor.GRAY + "Use /start para iniciar.");
            return true;
        }

        switch (sub) {
            case "nome":
                if (!sender.hasPermission("eventos.admin")) {
                    sender.sendMessage(ChatColor.RED + "Sem permissão.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.GOLD + "Nome atual do evento: " + ChatColor.WHITE + plugin.getEventManager().getNomeEventoAtual());
                    sender.sendMessage(ChatColor.YELLOW + "Uso: /lm nome <nome do evento>");
                    return true;
                }
                StringBuilder sb = new StringBuilder(args[1]);
                for (int i = 2; i < args.length; i++) sb.append(" ").append(args[i]);
                String nomeEvento = sb.toString();
                plugin.getEventManager().setNomeEventoAtual(nomeEvento);
                plugin.getVariablesManager().set("event.name", nomeEvento);
                sender.sendMessage(ChatColor.GREEN + "Nome do evento definido: " + nomeEvento);
                return true;

            case "tipo":
                if (!sender.hasPermission("eventos.admin")) {
                    sender.sendMessage(ChatColor.RED + "Sem permissão.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.GOLD + "Tipo atual: " + ChatColor.WHITE + plugin.getEventManager().getEventoTipo().name());
                    sender.sendMessage(ChatColor.YELLOW + "Uso: /lm tipo <solo|duplas|trio|panela>");
                    return true;
                }
                EventoTipo tipo = EventoTipo.parseTipo(args[1]);
                if (tipo == null) {
                    sender.sendMessage(ChatColor.RED + "Tipo inválido. Use solo, duplas, trio ou panela.");
                    return true;
                }
                plugin.getEventManager().setEventoTipo(tipo);
                plugin.getVariablesManager().set("event.persist_tipo", tipo.name());
                if (tipo.isEmTime()) {
                    plugin.getVariablesManager().set("event.max_por_clan", String.valueOf(tipo.getTamanhoTime()));
                } else {
                    plugin.getVariablesManager().set("event.max_por_clan", String.valueOf(EventEmbeddedConfig.MAX_JOGADORES_POR_CLAN));
                }
                sender.sendMessage(ChatColor.GREEN + "Tipo definido: " + ChatColor.WHITE + tipo.name()
                        + (tipo.isEmTime() ? ChatColor.GRAY + " (times de " + tipo.getTamanhoTime() + ")" : ""));
                return true;

            case "parar":
                if (!sender.hasPermission("eventos.admin")) {
                    sender.sendMessage(ChatColor.RED + "Sem permissão.");
                    return true;
                }
                plugin.getEventManager().pararEvento();
                sender.sendMessage(ChatColor.YELLOW + "Evento parado.");
                return true;

            case "status":
                EventState state = plugin.getEventManager().getState();
                EventoPronto pronto = plugin.getEventManager().getEventoProntoAtual();
                String modoArena = pronto != null ? pronto.getNome() + " §7(mapa gerado)" : "§7Schematic / resenha";
                sender.sendMessage(ChatColor.GOLD + "Estado: " + ChatColor.WHITE + state.name());
                sender.sendMessage(ChatColor.GOLD + "Arena: " + modoArena);
                sender.sendMessage(ChatColor.GOLD + "Tipo: " + ChatColor.WHITE + plugin.getEventManager().getEventoTipo().name());
                sender.sendMessage(ChatColor.GOLD + "Nome: " + ChatColor.WHITE + plugin.getEventManager().getNomeEventoAtual());
                sender.sendMessage(ChatColor.GOLD + "Variável event.name: " + ChatColor.WHITE + plugin.getVariablesManager().get("event.name", "(vazio)"));
                sender.sendMessage(ChatColor.GOLD + "Participantes: " + ChatColor.WHITE + plugin.getEventManager().getParticipantes().size());
                sender.sendMessage(ChatColor.GOLD + "Vivos: " + ChatColor.WHITE + plugin.getEventManager().getVivos().size());
                if (plugin.getEventManager().isCountdownParaIniciarRodando()) {
                    int t = plugin.getEventManager().getTempoRestanteParaIniciar();
                    sender.sendMessage(ChatColor.GOLD + "Countdown p/ iniciar: " + ChatColor.WHITE + t + " s");
                }
                return true;

            case "evento":
                if (!sender.hasPermission("eventos.admin")) {
                    sender.sendMessage(ChatColor.RED + "Sem permissão.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.YELLOW + "Uso: /lm evento list" + ChatColor.GRAY + " ou " + ChatColor.YELLOW + "/lm evento <1-" + plugin.getEventosPredefinidosManager().getQuantidade() + ">");
                    return true;
                }
                if (args[1].equalsIgnoreCase("list") || args[1].equalsIgnoreCase("lista")) {
                    sender.sendMessage(ChatColor.GOLD + "--- Eventos predefinidos ---");
                    int i = 1;
                    for (EventosPredefinidosManager.EventoPredefinido ev : plugin.getEventosPredefinidosManager().getEventos()) {
                        sender.sendMessage(ChatColor.GRAY + " " + i + ". " + ChatColor.WHITE + ev.getNome() + ChatColor.GRAY + " (" + ev.getTipo().name() + ")");
                        i++;
                    }
                    sender.sendMessage(ChatColor.GRAY + "Use /lm evento <número> para selecionar, depois /start para iniciar.");
                    return true;
                }
                int num;
                try {
                    num = Integer.parseInt(args[1]);
                } catch (NumberFormatException ex) {
                    sender.sendMessage(ChatColor.RED + "Número inválido. Use /lm evento list");
                    return true;
                }
                EventosPredefinidosManager.EventoPredefinido ev = plugin.getEventosPredefinidosManager().getPorNumero(num);
                if (ev == null) {
                    sender.sendMessage(ChatColor.RED + "Evento inválido. Use /lm evento list (1-" + plugin.getEventosPredefinidosManager().getQuantidade() + ")");
                    return true;
                }
                plugin.getEventManager().setNomeEventoAtual(ev.getNome());
                plugin.getEventManager().setEventoTipo(ev.getTipo());
                plugin.getVariablesManager().set("event.name", ev.getNome());
                plugin.getVariablesManager().set("event.persist_tipo", ev.getTipo().name());
                if (ev.getTipo().isEmTime()) {
                    plugin.getVariablesManager().set("event.max_por_clan", String.valueOf(ev.getTipo().getTamanhoTime()));
                } else {
                    plugin.getVariablesManager().set("event.max_por_clan", String.valueOf(EventEmbeddedConfig.MAX_JOGADORES_POR_CLAN));
                }
                sender.sendMessage(ChatColor.GREEN + "Evento selecionado: " + ChatColor.WHITE + ev.getNome() + ChatColor.GRAY + " (" + ev.getTipo().name() + ")");
                sender.sendMessage(ChatColor.GRAY + "Use /start para iniciar.");
                return true;

            default:
                enviarAjuda(sender);
                return true;
        }
    }

    private void enviarAjuda(CommandSender sender) {
        sender.sendMessage(EventUiTheme.FRAME);
        sender.sendMessage("§e§l/lm §7— §fdefinir evento antes do §e/start");
        sender.sendMessage("");
        if (sender.hasPermission("eventos.admin")) {
            sender.sendMessage("§e/lm nome §8· §7nome do scoreboard/evento");
            sender.sendMessage("§e/lm tipo §8· §7solo §8| §7duplas §8| §7trio §8| §7panela");
            sender.sendMessage("§e/lm parar §8· §7encerra o evento");
        }
        sender.sendMessage("§e/lm evento list §8· §7lista predefinidos");
        sender.sendMessage("§e/lm evento §7<n> §8· §7escolher slot §8(§71§8-§75§8)");
        sender.sendMessage("§e/lm resenha §8| §epadrao §8· §7só schematic (sem mapa gerado)");
        sender.sendMessage("§7Modos com arena§8: §fspleef §8· §ffolha §8· §fsumo §8· §flavarun §8· §ftntrun §8· §fvidro");
        sender.sendMessage("§e/lm status §8· §7estado e contagens");
        sender.sendMessage("");
        sender.sendMessage("§7Outros§8: §f/start §8· §f/time §8· §f/evento §8· §f/grupo §8· §f/evtreload");
        sender.sendMessage(EventUiTheme.FRAME);
    }
}
