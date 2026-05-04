package com.minecraft.arcade.eventos.listeners;

import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.managers.EventCosmeticsManager;
import com.minecraft.arcade.eventos.managers.EventManager;
import com.minecraft.arcade.eventos.managers.EventState;
import com.minecraft.arcade.eventos.settings.EventEmbeddedConfig;
import com.minecraft.arcade.eventos.utils.EventParticipacaoUtil;
import com.minecraft.arcade.eventos.utils.TitleUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventoListener implements Listener {

    private final Eventos plugin;

    public EventoListener() {
        this(Eventos.getPlugin());
    }

    public EventoListener(Eventos plugin) {
        this.plugin = plugin;
    }

    private boolean isVarTrue(String key) {
        String v = plugin.getVariablesManager().get(key, "true");
        return v == null || v.isEmpty() || v.equalsIgnoreCase("true") || v.equalsIgnoreCase("1");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncPlayerChatEvent e) {
        if (plugin.getEventManager().getState() != EventState.EM_JOGO) return;
        if (!plugin.getEventManager().isParticipando(e.getPlayer().getUniqueId())) return;
        if (!isVarTrue("event.chat")) {
            e.setCancelled(true);
        }
    }

    /** Chat com tag + medalhas + corpo ({@code event.chat_corpo}, placeholders %1$s %2$s). */
    @EventHandler(priority = EventPriority.HIGH)
    public void onChatFormatCosmeticos(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) return;
        if (!plugin.getVariablesManager().getBoolean("event.cosmetics_chat", true)) return;

        org.bukkit.entity.Player p = e.getPlayer();
        EventManager em = plugin.getEventManager();
        EventState st = em.getState();
        if (st != EventState.CONTAGEM && st != EventState.EM_JOGO && st != EventState.AREA_BOLO) return;
        if (!em.isParticipando(p.getUniqueId())) return;

        EventCosmeticsManager cos = plugin.getEventCosmeticsManager();
        if (cos == null) return;

        String prefixo = cos.resolverTag(p, em) + cos.formatarListaMedalsParaMensagens(p);
        String corpo = plugin.getVariablesManager().get("event.chat_corpo", "&f%1$s&8:&r &7%2$s");
        e.setFormat(prefixo + org.bukkit.ChatColor.translateAlternateColorCodes('&', corpo));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (plugin.getEventManager().getState() != EventState.EM_JOGO) return;
        if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player)) return;
        Player damaged = (Player) e.getEntity();
        Player damager = (Player) e.getDamager();
        if (plugin.getEventManager().isEspectador(damaged.getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        if (!plugin.getEventManager().isParticipando(damaged.getUniqueId())) return;
        if (!plugin.getEventManager().isParticipando(damager.getUniqueId())) return;
        if (plugin.getEventManager().isDentroInvencibilidade()) {
            e.setCancelled(true);
            return;
        }
        if (!isVarTrue("event.pvp")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent e) {
        if (plugin.getEventManager().getState() != EventState.EM_JOGO) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (plugin.getEventManager().isEspectador(p.getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        if (!plugin.getEventManager().isParticipando(p.getUniqueId())) return;
        if (plugin.getEventManager().isDentroInvencibilidade()) {
            e.setCancelled(true);
            return;
        }
        if (!isVarTrue("event.damage")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onHunger(FoodLevelChangeEvent e) {
        if (plugin.getEventManager().getState() != EventState.EM_JOGO) return;
        if (!(e.getEntity() instanceof Player)) return;
        if (!plugin.getEventManager().isParticipando(e.getEntity().getUniqueId())) return;
        if (!isVarTrue("event.hunger")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (plugin.getEventManager().getState() != EventState.EM_JOGO) return;
        if (plugin.getEventManager().isEspectador(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        if (!plugin.getEventManager().isParticipando(e.getPlayer().getUniqueId())) return;
        if (!isVarTrue("event.blockplace")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent e) {
        if (plugin.getEventManager().getState() != EventState.EM_JOGO) return;
        if (plugin.getEventManager().isEspectador(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDeath(PlayerDeathEvent e) {
        if (plugin.getEventManager().getState() != EventState.EM_JOGO) return;
        Player morto = e.getEntity();
        if (!plugin.getEventManager().isParticipando(morto.getUniqueId())) return;

        Player killer = morto.getKiller();
        if (killer != null
                && plugin.getEventManager().isParticipando(killer.getUniqueId())
                && plugin.getEventManager().isVivo(killer.getUniqueId())
                && !plugin.getEventManager().isEspectador(killer.getUniqueId())) {
            plugin.getEventManager().registrarKill(killer.getUniqueId(), morto.getName());
        }

        plugin.getEventManager().jogadorMorreu(morto);

        // Auto-respawn durante o evento (sai da tela de morte na hora)
        if (plugin.getVariablesManager().getBoolean("event.auto_respawn", true)) {
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (morto.isOnline()) morto.spigot().respawn();
            }, 2L);
        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        org.bukkit.entity.Player player = e.getPlayer();
        java.util.UUID uuid = player.getUniqueId();
        if (plugin.getEventManager().getState() == EventState.EM_JOGO
                && plugin.getEventManager().isParticipando(uuid)
                && player.hasPermission("eventos.vip")) {
            plugin.getEventManager().registrarQuitRelog(uuid);
        } else {
            plugin.getEventManager().getParticipantes().remove(uuid);
            plugin.getEventManager().getVivos().remove(uuid);
        }
        plugin.getEventManager().getEspectadores().remove(uuid);
        plugin.getClanEventManager().sairDoEvento(uuid);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        String titulo = plugin.getVariablesManager().get("event.join_titulo", EventEmbeddedConfig.MSG_JOIN_TITULO);
        String subtitulo = plugin.getVariablesManager().get("event.join_subtitulo", EventEmbeddedConfig.MSG_JOIN_SUBTITULO);
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () ->
                TitleUtil.sendTitle(p, titulo, subtitulo, 10, 70, 20), 20L);
        if (p.hasPermission("eventos.jogar") && plugin.getEventScoreboard() != null) {
            plugin.getEventScoreboard().mostrarPara(p);
        }
        if (plugin.getEventManager().getState() == EventState.CONTAGEM) {
            Bukkit.getScheduler().runTask(plugin, () -> plugin.getEventManager().sincronizarIngressoComServidorEVanish());
        }
        if (plugin.getEventManager().getState() == EventState.EM_JOGO && plugin.getEventManager().isEmRelog(p.getUniqueId())) {
            if (plugin.getEventManager().readicionarVivoApósRelog(p.getUniqueId())) {
                org.bukkit.Location spawn = plugin.getEventManager().getSpawnAtualEvento();
                if (spawn != null) p.teleport(spawn);
                p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                        plugin.getVariablesManager().get("event.msg_relog_voltou", EventEmbeddedConfig.MSG_RELOG_VOLTOU))));
            }
        }
        // Auto-início: quando há 5+ jogadores no lobby, inicia countdown de 5 min
                if (plugin.getEventManager().getState() == EventState.AGUARDANDO
                && EventParticipacaoUtil.contaComoJogadorElegivelEvento(p)
                && !plugin.getEventManager().isCountdownParaIniciarRodando()) {
            int min = plugin.getVariablesManager().getInt("event.min_players", EventEmbeddedConfig.MIN_JOGADORES_AUTO_INICIO);
            if (plugin.getEventManager().getJogadoresOnlineComPermissao() >= min) {
                int segundos = plugin.getVariablesManager().getInt("event.tempo_iniciar", EventEmbeddedConfig.TEMPO_PARA_INICIAR_SEGUNDOS);
                plugin.getEventManager().iniciarCountdownParaIniciar(segundos);
            }
        }
    }
}
