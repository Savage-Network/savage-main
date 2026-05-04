package com.minecraft.arcade.eventos.managers;

import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.eventos.EventoPronto;
import com.minecraft.arcade.eventos.kits.EventKitApplier;
import com.minecraft.arcade.eventos.settings.EventEmbeddedConfig;
import com.minecraft.arcade.eventos.utils.EventParticipacaoUtil;
import com.minecraft.arcade.eventos.utils.TitleUtil;
import com.minecraft.core.bukkit.api.vanish.Vanish;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager {

    private final Eventos plugin;
    private EventState state = EventState.AGUARDANDO;
    private final Set<UUID> participantes = ConcurrentHashMap.newKeySet();
    private final Set<UUID> vivos = ConcurrentHashMap.newKeySet();
    private final Set<UUID> espectadores = ConcurrentHashMap.newKeySet();
    private BukkitTask contagemTask;
    private BukkitTask countdownParaIniciarTask;
    private BukkitTask boloTask;
    private String nomeEventoAtual = "Evento";
    private UUID vencedor;
    private EventoTipo eventoTipo = EventoTipo.SOLO;
    /** Quando evento é em time, todos os UUIDs do time vencedor. */
    private Set<UUID> vencedoresTime;
    /** Evento pronto com mapa gerado (spleef, folha, sumo, lavarun, tntrun, vidro). Se null, usa schematic. */
    private EventoPronto eventoProntoAtual;
    /** Spawn da arena já carregada com /lm (para não gerar de novo no /start). */
    private Location spawnEventoPreparado;
    /** Quem saiu durante EM_JOGO com eventos.vip: UUID -> timestamp de quando saiu (para relog time). */
    private final Map<UUID, Long> relogQuitTime = new ConcurrentHashMap<>();
    private BukkitTask relogTask;
    /** Momento em que o jogo iniciou (EM_JOGO) para calcular invencibilidade. */
    private long jogoInicioTimeMillis;
    /** Spawn atual do evento (para teleportar quem volta do relog). */
    private Location spawnAtualEvento;
    /** Tempo restante em segundos do countdown para iniciar (5 min). -1 se não estiver rodando. */
    private volatile int tempoRestanteParaIniciar = -1;
    /** Tempo restante em segundos da contagem regressiva (10s antes do GO). -1 se não estiver rodando. */
    private volatile int tempoRestanteContagem = -1;

    private static final float VELOCIDADE_CAMINHADA_PADRAO = 0.2f;

    /** Eliminações atribuídas ao killer na rodada (listener + morte por queda no modo vidro não conta killer). */
    private final Map<UUID, Integer> killsNaRodada = new ConcurrentHashMap<>();

    public EventManager(Eventos plugin) {
        this.plugin = plugin;
    }

    public EventoTipo getEventoTipo() {
        return eventoTipo;
    }

    /** Define o tipo do evento (solo, duplas, trio, panela). */
    public void setEventoTipo(EventoTipo eventoTipo) {
        this.eventoTipo = eventoTipo != null ? eventoTipo : EventoTipo.SOLO;
    }

    public EventState getState() {
        return state;
    }

    public void setState(EventState state) {
        this.state = state;
    }

    public Set<UUID> getParticipantes() {
        return participantes;
    }

    public Set<UUID> getVivos() {
        return vivos;
    }

    public Set<UUID> getEspectadores() {
        return espectadores;
    }

    public String getNomeEventoAtual() {
        return nomeEventoAtual;
    }

    public void setNomeEventoAtual(String nomeEventoAtual) {
        this.nomeEventoAtual = nomeEventoAtual;
    }

    public UUID getVencedor() {
        return vencedor;
    }

    public boolean isParticipando(UUID uuid) {
        return participantes.contains(uuid);
    }

    public boolean isVivo(UUID uuid) {
        return vivos.contains(uuid);
    }

    public boolean isEspectador(UUID uuid) {
        return espectadores.contains(uuid);
    }

    public int getKills(UUID uuid) {
        return killsNaRodada.getOrDefault(uuid, 0);
    }

    public List<Map.Entry<UUID, Integer>> getKillsCopiaParaRanking() {
        return new ArrayList<>(killsNaRodada.entrySet());
    }

    /** Regista uma eliminação PvP (chamado pelo listener antes de {@link #jogadorMorreu(Player)}). */
    public void registrarKill(UUID assassino, String nomeVitimaParaBroadcast) {
        if (assassino == null || !participantes.contains(assassino) || !vivos.contains(assassino) || espectadores.contains(assassino)) {
            return;
        }
        killsNaRodada.merge(assassino, 1, Integer::sum);
        if (nomeVitimaParaBroadcast != null && plugin.getVariablesManager().getBoolean("event.broadcast_kill", false)) {
            String a = Optional.ofNullable(Bukkit.getPlayer(assassino)).map(Player::getName).orElse("?");
            Bukkit.broadcastMessage(ChatColor.GRAY + a + ChatColor.DARK_GRAY + " ➟ " + ChatColor.STRIKETHROUGH + nomeVitimaParaBroadcast);
        }
    }

    /**
     * Linha curta para scoreboard durante {@link EventState#EM_JOGO} (liderança por eliminações).
     */
    public String getLinhaRankingKillsScoreboard() {
        if (killsNaRodada.isEmpty()) {
            return "§7Kills§8: §f0";
        }
        UUID best = null;
        int max = -1;
        for (Map.Entry<UUID, Integer> e : killsNaRodada.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                best = e.getKey();
            }
        }
        if (best == null) return "§7Kills§8: §f0";
        String nm = Optional.ofNullable(Bukkit.getOfflinePlayer(best).getName()).orElse("?");
        if (nm.length() > 10) nm = nm.substring(0, 10);
        return "§7Top§8: §f" + nm + " §8(§f" + max + "§8)";
    }

    /**
     * Entrada tardia na {@link EventState#CONTAGEM}. Códigos: 0 ok, 1 estado errado, 2 já inscrito,
     * 3 lotação, 4 sem teleport, {@code 5} staff em vanish (não entra até sair do vanish ou ser detectado pela sinc.).
     */
    public int entrarNaContagem(Player p) {
        if (state != EventState.CONTAGEM) return 1;
        if (Vanish.has(p)) return 5;
        if (participantes.contains(p.getUniqueId())) return 2;
        int max = plugin.getVariablesManager().getInt("event.max_players", EventEmbeddedConfig.MAX_JOGADORES_EVENTO);
        if (participantes.size() >= max) return 3;
        Location base = spawnAtualEvento;
        if (base == null) {
            base = plugin.getSchematicManager().getSpawnEvento();
        }
        if (base == null || base.getWorld() == null) return 4;
        Location dest = base.clone().add(0, 1, 0);
        registrarParticipanteNaContagemComTeleport(p, dest);
        return 0;
    }

    /** Repõe teleporte/preparação de contagem (join automático ou após tirar vanish). */
    private void registrarParticipanteNaContagemComTeleport(Player p, Location dest) {
        UUID id = p.getUniqueId();
        participantes.add(id);
        vivos.add(id);
        p.getInventory().clear();
        p.setHealth(p.getMaxHealth());
        p.setFoodLevel(20);
        p.teleport(dest);
        p.setGameMode(GameMode.SURVIVAL);
        if (plugin.getVariablesManager().getBoolean("event.freeze_contagem", false)) {
            p.setWalkSpeed(0f);
        }
        plugin.getEventScoreboard().mostrarPara(p);
        Bukkit.getScheduler().runTask(plugin, () -> {
            EventCosmeticsManager cx = plugin.getEventCosmeticsManager();
            if (cx != null && p.isOnline()) {
                cx.refreshPlayer(p);
            }
        });
    }

    private void restaurarVelocidadeParticipantesAtuais() {
        for (UUID u : participantes) {
            Player pl = Bukkit.getPlayer(u);
            if (pl != null && pl.isOnline()) {
                pl.setWalkSpeed(VELOCIDADE_CAMINHADA_PADRAO);
            }
        }
    }

    private void aplicarCongelamentoContagemSeAtivo() {
        if (!plugin.getVariablesManager().getBoolean("event.freeze_contagem", false)) return;
        for (UUID uuid : participantes) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.setWalkSpeed(0f);
            }
        }
    }

    private void celebrarVitóriaComFogos(Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        if (!plugin.getVariablesManager().getBoolean("event.firework_vitoria", true)) return;
        Location l = loc.clone().add(0, 1, 0);
        try {
            Firework fw = (Firework) l.getWorld().spawnEntity(l, EntityType.FIREWORK);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                    .with(FireworkEffect.Type.BURST)
                    .withColor(Color.YELLOW, Color.ORANGE)
                    .withFade(Color.WHITE)
                    .flicker(true)
                    .trail(false)
                    .build());
            meta.setPower(1);
            fw.setFireworkMeta(meta);
        } catch (Throwable ignored) {
        }
    }

    public EventoPronto getEventoProntoAtual() {
        return eventoProntoAtual;
    }

    /** Minutos de relog para quem tem permissão eventos.vip (variável event.relog_minutos, fallback config). */
    public int getRelogTimeMinutos() {
        return plugin.getVariablesManager().getInt("event.relog_minutos", EventEmbeddedConfig.RELOG_TIME_MINUTOS);
    }

    /** Segundos de invencibilidade no início do jogo (variável event.invencibilidade, fallback config). */
    public int getTempoInvencibilidadeSegundos() {
        return plugin.getVariablesManager().getInt("event.invencibilidade", EventEmbeddedConfig.TEMPO_INVENCIBILIDADE_SEGUNDOS);
    }

    /** Retorna true se o jogador está dentro do período de invencibilidade (após início do evento). */
    public boolean isDentroInvencibilidade() {
        if (state != EventState.EM_JOGO || jogoInicioTimeMillis <= 0) return false;
        long elapsedSec = (System.currentTimeMillis() - jogoInicioTimeMillis) / 1000;
        return elapsedSec < getTempoInvencibilidadeSegundos();
    }

    /** Registra que o jogador saiu e pode relogar (tem permissão eventos.vip). Remove dos vivos. */
    public void registrarQuitRelog(UUID uuid) {
        relogQuitTime.put(uuid, System.currentTimeMillis());
        vivos.remove(uuid);
        iniciarTaskRelog();
    }

    /** Retorna true se o UUID está na janela de relog (saiu e ainda pode voltar). */
    public boolean isEmRelog(UUID uuid) {
        return relogQuitTime.containsKey(uuid);
    }

    /** Remove da janela de relog e retorna true se estava dentro do tempo. Se voltar a tempo, chame readicionarVivoApósRelog. */
    public boolean readicionarVivoApósRelog(UUID uuid) {
        Long quitAt = relogQuitTime.remove(uuid);
        if (quitAt == null) return false;
        int min = getRelogTimeMinutos();
        if ((System.currentTimeMillis() - quitAt) / 60000L > min) return false;
        vivos.add(uuid);
        return true;
    }

    public Location getSpawnAtualEvento() {
        return spawnAtualEvento;
    }

    private void iniciarTaskRelog() {
        if (relogTask != null) return;
        relogTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (state != EventState.EM_JOGO && state != EventState.CONTAGEM) {
                    relogQuitTime.clear();
                    cancel();
                    relogTask = null;
                    return;
                }
                int min = getRelogTimeMinutos();
                long now = System.currentTimeMillis();
                List<UUID> expirados = new ArrayList<>();
                for (Map.Entry<UUID, Long> e : relogQuitTime.entrySet()) {
                    if ((now - e.getValue()) / 60000L > min) expirados.add(e.getKey());
                }
                for (UUID u : expirados) {
                    relogQuitTime.remove(u);
                    participantes.remove(u);
                    vivos.remove(u);
                    verificarVitoriaAposSaida();
                }
                if (relogQuitTime.isEmpty()) {
                    cancel();
                    relogTask = null;
                }
            }
        }.runTaskTimer(plugin, 100L, 100L);
    }

    /** Verifica condição de vitória (1 vivo ou times) após alguém sair/expirar relog. */
    private void verificarVitoriaAposSaida() {
        if (eventoTipo == EventoTipo.SOLO) {
            if (vivos.size() == 1) {
                vencedor = vivos.iterator().next();
                finalizarEvento();
            } else if (vivos.isEmpty()) {
                finalizarEvento();
            }
        } else {
            Set<String> timesComVivos = new HashSet<>();
            for (UUID vivo : vivos) {
                String clan = plugin.getClanEventManager().getClan(vivo);
                timesComVivos.add(clan != null ? clan : vivo.toString());
            }
            if (timesComVivos.size() == 1) {
                String timeVencedor = timesComVivos.iterator().next();
                vencedoresTime = new HashSet<>();
                for (UUID p : participantes) {
                    String c = plugin.getClanEventManager().getClan(p);
                    String teamId = c != null ? c : p.toString();
                    if (teamId.equals(timeVencedor)) {
                        vencedoresTime.add(p);
                    }
                }
                vencedor = vencedoresTime.iterator().next();
                finalizarEvento();
            } else if (timesComVivos.isEmpty()) {
                finalizarEvento();
            }
        }
    }

    /**
     * Staff em vanish deixa os conjuntos do evento; na contagem, volta a ingressar ao sair do vanish.
     * Quorum de auto-início usa só jogadores elegíveis (ver {@link EventParticipacaoUtil}).
     */
    public void sincronizarIngressoComServidorEVanish() {
        EventState st = state;
        if (st != EventState.CONTAGEM && st != EventState.EM_JOGO && st != EventState.AREA_BOLO) return;

        boolean mudou = false;
        HashSet<UUID> retiradosPorVanish = new HashSet<>();
        for (UUID u : new HashSet<>(participantes)) {
            Player p = Bukkit.getPlayer(u);
            if (p == null || !p.isOnline()) continue;
            if (Vanish.has(p)) {
                retiradosPorVanish.add(u);
            }
        }
        boolean checarVitória = false;
        for (UUID u : retiradosPorVanish) {
            mudou = true;
            boolean eraVivo = vivos.contains(u);
            participantes.remove(u);
            vivos.remove(u);
            espectadores.remove(u);
            relogQuitTime.remove(u);
            killsNaRodada.remove(u);
            plugin.getClanEventManager().sairDoEvento(u);
            if (eraVivo && st == EventState.EM_JOGO) checarVitória = true;
        }
        if (checarVitória) {
            verificarVitoriaAposSaida();
        }

        if (state == EventState.CONTAGEM) {
            int max = plugin.getVariablesManager().getInt("event.max_players", EventEmbeddedConfig.MAX_JOGADORES_EVENTO);
            Location base = spawnAtualEvento;
            if (base == null) {
                base = plugin.getSchematicManager().getSpawnEvento();
            }
            if (base != null && base.getWorld() != null && participantes.size() < max) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (participantes.size() >= max) break;
                    if (!EventParticipacaoUtil.contaComoJogadorElegivelEvento(p)) continue;
                    if (participantes.contains(p.getUniqueId())) continue;
                    registrarParticipanteNaContagemComTeleport(p, base.clone().add(0, 1, 0));
                    mudou = true;
                }
            }
        }

        if (mudou) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                EventCosmeticsManager cx = plugin.getEventCosmeticsManager();
                if (cx != null) cx.refreshEmEventoTodos();
            });
        }
    }

    /** Jogadores visíveis com {@code eventos.jogar} (exclui staff em vanish). Usado para fila/auto-início. */
    public int getJogadoresOnlineComPermissao() {
        int n = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (EventParticipacaoUtil.contaComoJogadorElegivelEvento(p)) n++;
        }
        return n;
    }

    /** Retorna true se o countdown para iniciar o evento está rodando. */
    public boolean isCountdownParaIniciarRodando() {
        return countdownParaIniciarTask != null;
    }

    /** Tempo restante em segundos do countdown para iniciar. -1 se não estiver rodando. */
    public int getTempoRestanteParaIniciar() {
        return tempoRestanteParaIniciar;
    }

    /** Tempo restante em segundos da contagem regressiva (antes do GO). -1 se não estiver rodando. */
    public int getTempoRestanteContagemRegressiva() {
        return tempoRestanteContagem;
    }

    /** Inicia o countdown (em segundos) para iniciar o evento. Ao chegar em 0 chama {@link #iniciarEvento(String)}. */
    public void iniciarCountdownParaIniciar(int segundos) {
        if (state != EventState.AGUARDANDO) {
            return;
        }
        pararCountdownParaIniciar();
        if (segundos <= 0) {
            if (!iniciarEvento(nomeEventoAtual)) {
                Bukkit.broadcastMessage(ChatColor.GRAY + "Início automático cancelado (sem jogadores com eventos.jogar ou problema ao carregar spawn).");
            }
            return;
        }
        plugin.getVariablesManager().set("event.tempo_iniciar", String.valueOf(segundos));

        final int minJogadores = plugin.getVariablesManager().getInt("event.min_players", EventEmbeddedConfig.MIN_JOGADORES_AUTO_INICIO);
        countdownParaIniciarTask = new BukkitRunnable() {
            int tempoRestante = segundos;

            @Override
            public void run() {
                if (state != EventState.AGUARDANDO || plugin.getEventManager().getJogadoresOnlineComPermissao() < minJogadores) {
                    tempoRestanteParaIniciar = -1;
                    cancel();
                    countdownParaIniciarTask = null;
                    if (state == EventState.AGUARDANDO) {
                        Bukkit.broadcastMessage(ChatColor.GRAY + "Countdown cancelado (menos de " + minJogadores + " jogadores).");
                    }
                    return;
                }
                tempoRestanteParaIniciar = tempoRestante;
                if (tempoRestante <= 0) {
                    tempoRestanteParaIniciar = -1;
                    countdownParaIniciarTask = null;
                    cancel();
                    if (!iniciarEvento(nomeEventoAtual)) {
                        Bukkit.broadcastMessage(ChatColor.GRAY + "Contagem zerou mas o evento não iniciou (sem jogadores elegíveis ou spawn indisponível).");
                    }
                    return;
                }
                if (tempoRestante <= 10 || tempoRestante % 60 == 0) {
                    String msg = plugin.getVariablesManager().get("event.msg_evento_iniciando", EventEmbeddedConfig.MSG_EVENTO_INICIANDO)
                            .replace("%tempo%", String.valueOf(tempoRestante));
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));
                }
                tempoRestante--;
            }
        }.runTaskTimer(plugin, 20L, 20L);

        String msgInicio = plugin.getVariablesManager().get("event.msg_countdown_iniciado", EventEmbeddedConfig.MSG_COUNTDOWN_INICIADO)
                .replace("%tempo%", String.valueOf(segundos));
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msgInicio));
    }

    /** Para o countdown para iniciar o evento. */
    public void pararCountdownParaIniciar() {
        if (countdownParaIniciarTask != null) {
            countdownParaIniciarTask.cancel();
            countdownParaIniciarTask = null;
            tempoRestanteParaIniciar = -1;
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Countdown para iniciar o evento foi parado.");
        }
    }

    public void setEventoProntoAtual(EventoPronto eventoProntoAtual) {
        this.eventoProntoAtual = eventoProntoAtual;
    }

    /** Limpa a arena atual (unregister + limparMapa) e zera spawnEventoPreparado. Usado ao trocar de evento no /lm. */
    public void limparArenaAtual() {
        if (eventoProntoAtual != null) {
            eventoProntoAtual.unregisterListeners();
            eventoProntoAtual.limparMapa();
        }
        spawnEventoPreparado = null;
    }

    /**
     * Carrega a arena do evento atual na origem do spawn-evento (chamado no /lm para já deixar a arena pronta).
     * Retorna a Location de spawn ou null se não houver evento com mapa.
     */
    public Location prepararArenaAgora() {
        if (eventoProntoAtual == null) return null;
        Location origin = plugin.getSchematicManager().getSpawnEvento();
        if (origin == null || origin.getWorld() == null) return null;
        spawnEventoPreparado = eventoProntoAtual.prepararMapa(origin.getWorld(), origin);
        return spawnEventoPreparado;
    }

    /**
     * Inicia a contagem regressiva até o GO. Falha sem alterar estado se não há jogadores elegíveis ou spawn inválido.
     *
     * @return {@code true} se a contagem começou
     */
    public boolean iniciarEvento(String nomeEvento) {
        if (state != EventState.AGUARDANDO) {
            return false;
        }
        if (getJogadoresOnlineComPermissao() <= 0) {
            return false;
        }

        Location spawnEvento;
        if (eventoProntoAtual != null) {
            if (spawnEventoPreparado != null) {
                spawnEvento = spawnEventoPreparado;
            } else {
                Location origin = plugin.getSchematicManager().getSpawnEvento();
                if (origin == null || origin.getWorld() == null) {
                    plugin.getLogger().warning("[Eventos] Origem/schematic inválida — não foi possível gerar o mapa do modo pronto.");
                    return false;
                }
                spawnEvento = eventoProntoAtual.prepararMapa(origin.getWorld(), origin);
            }
            if (spawnEvento == null || spawnEvento.getWorld() == null) {
                plugin.getLogger().warning("[Eventos] Spawn do modo pronto indisponível.");
                return false;
            }
        } else {
            plugin.getSchematicManager().carregarMapaEvento();
            spawnEvento = plugin.getSchematicManager().getSpawnEvento();
            if (spawnEvento == null || spawnEvento.getWorld() == null) {
                plugin.getLogger().warning("[Eventos] Spawn do schematic principal indisponível.");
                return false;
            }
        }

        nomeEventoAtual = (nomeEvento != null && !nomeEvento.trim().isEmpty()) ? nomeEvento.trim() : "Evento";
        plugin.getVariablesManager().set("event.name", nomeEventoAtual);
        killsNaRodada.clear();

        state = EventState.CONTAGEM;
        participantes.clear();
        vivos.clear();
        espectadores.clear();
        vencedor = null;
        vencedoresTime = null;

        if (eventoProntoAtual != null) {
            eventoProntoAtual.registerListeners(plugin);
        }

        final Location spawnFinal = spawnEvento.clone().add(0, 1, 0);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (EventParticipacaoUtil.contaComoJogadorElegivelEvento(p)) {
                participantes.add(p.getUniqueId());
                vivos.add(p.getUniqueId());
            }
        }
        if (eventoProntoAtual != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (UUID uuid : participantes) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null && p.isOnline()) {
                        p.teleport(spawnFinal);
                        p.getInventory().clear();
                        p.setGameMode(GameMode.SURVIVAL);
                        p.setHealth(p.getMaxHealth());
                        p.setFoodLevel(20);
                    }
                }
                spawnAtualEvento = spawnFinal.clone();
            }, 5L);
        } else {
            for (UUID uuid : participantes) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    p.teleport(spawnFinal);
                    p.getInventory().clear();
                    p.setGameMode(GameMode.SURVIVAL);
                    p.setHealth(p.getMaxHealth());
                    p.setFoodLevel(20);
                }
            }
            spawnAtualEvento = spawnFinal.clone();
        }

        int segundos = plugin.getVariablesManager().getInt("event.contagem_inicial", EventEmbeddedConfig.CONTAGEM_REGRESSIVA_INICIAL);
        iniciarContagemRegressiva(segundos);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            EventCosmeticsManager cx = plugin.getEventCosmeticsManager();
            if (cx != null) {
                cx.refreshEmEventoTodos();
            }
        }, 8L);
        return true;
    }

    private void iniciarContagemRegressiva(int segundos) {
        tempoRestanteContagem = segundos;
        contagemTask = new BukkitRunnable() {
            int tempo = segundos;

            @Override
            public void run() {
                if (tempo <= 0) {
                    tempoRestanteContagem = -1;
                    cancel();
                    iniciarJogo();
                    return;
                }
                tempoRestanteContagem = tempo;
                String msg = plugin.getVariablesManager().get("event.msg_evento_iniciando", EventEmbeddedConfig.MSG_EVENTO_INICIANDO)
                        .replace("%tempo%", String.valueOf(tempo));
                String tituloContagem = plugin.getVariablesManager().get("event.titulo_contagem", EventEmbeddedConfig.MSG_TITULO_CONTAGEM)
                        .replace("%tempo%", String.valueOf(tempo));
                String subtituloContagem = plugin.getVariablesManager().get("event.titulo_contagem_subtitulo", EventEmbeddedConfig.MSG_TITULO_CONTAGEM_SUB);
                for (UUID uuid : participantes) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null && p.isOnline()) {
                        TitleUtil.sendTitle(p, tituloContagem, subtituloContagem, 5, 20, 5);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                        p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 1f);
                    }
                }
                tempo--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        aplicarCongelamentoContagemSeAtivo();
        Bukkit.getScheduler().runTaskLater(plugin, this::aplicarCongelamentoContagemSeAtivo, 10L);
    }

    private void iniciarJogo() {
        state = EventState.EM_JOGO;
        jogoInicioTimeMillis = System.currentTimeMillis();
        restaurarVelocidadeParticipantesAtuais();
        String msg = plugin.getVariablesManager().get("event.msg_evento_iniciado", EventEmbeddedConfig.MSG_EVENTO_INICIADO);
        for (UUID uuid : participantes) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                TitleUtil.sendTitle(p, "&aGO!", "&7Boa sorte!");
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                EventKitApplier.aplicarAoInicioDeJogo(p, plugin.getVariablesManager());
            }
        }
        EventCosmeticsManager cc = plugin.getEventCosmeticsManager();
        if (cc != null) {
            cc.refreshEmEventoTodos();
        }
    }

    public void jogadorMorreu(Player morto) {
        UUID uuid = morto.getUniqueId();
        if (!vivos.contains(uuid)) return;
        vivos.remove(uuid);
        espectadores.add(uuid);

        Bukkit.getScheduler().runTask(plugin, () -> {
            EventCosmeticsManager cx = plugin.getEventCosmeticsManager();
            if (cx != null) {
                cx.refreshEmEventoTodos();
            }
        });

        morto.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getVariablesManager().get("event.msg_espectador", EventEmbeddedConfig.MSG_ESPECTADOR))));
        morto.sendMessage(ChatColor.GRAY + "Use " + ChatColor.YELLOW + "/espectador" + ChatColor.GRAY + " para abrir o menu (teleportar / lobby).");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!morto.isOnline() || state != EventState.EM_JOGO) return;
            morto.setGameMode(GameMode.SURVIVAL);
            morto.setHealth(morto.getMaxHealth());
            morto.setFoodLevel(20);
            morto.setAllowFlight(true);
            morto.setFlying(true);
            morto.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true));
            morto.getInventory().setItem(0, null);
            morto.getInventory().setItem(8, criarItemEspectador(Material.BED, "§cIr ao Lobby"));
            morto.updateInventory();
        }, 5L);

        if (eventoTipo == EventoTipo.SOLO) {
            if (vivos.size() == 1) {
                vencedor = vivos.iterator().next();
                finalizarEvento();
            } else if (vivos.isEmpty()) {
                finalizarEvento();
            }
        } else {
            // Modo time: verificar quantos times ainda têm alguém vivo
            Set<String> timesComVivos = new HashSet<>();
            for (UUID vivo : vivos) {
                String clan = plugin.getClanEventManager().getClan(vivo);
                timesComVivos.add(clan != null ? clan : vivo.toString());
            }
            if (timesComVivos.size() == 1) {
                String timeVencedor = timesComVivos.iterator().next();
                vencedoresTime = new HashSet<>();
                for (UUID p : participantes) {
                    String c = plugin.getClanEventManager().getClan(p);
                    String teamId = c != null ? c : p.toString();
                    if (teamId.equals(timeVencedor)) {
                        vencedoresTime.add(p);
                    }
                }
                vencedor = vencedoresTime.iterator().next();
                finalizarEvento();
            } else if (timesComVivos.isEmpty()) {
                finalizarEvento();
            }
        }
    }

    private static ItemStack criarItemEspectador(Material type, String displayName) {
        ItemStack item = new ItemStack(type, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
        }
        item.setItemMeta(meta);
        return item;
    }

    private void finalizarEvento() {
        EventCosmeticsManager cos = plugin.getEventCosmeticsManager();
        if (cos != null) {
            cos.concederMedalhasFimRodada(
                    new HashSet<>(participantes),
                    vencedor,
                    vencedoresTime == null ? Collections.emptySet() : new HashSet<>(vencedoresTime),
                    new HashMap<>(killsNaRodada));
        }
        if (eventoProntoAtual != null) {
            eventoProntoAtual.unregisterListeners();
            eventoProntoAtual.limparMapa();
            eventoProntoAtual = null;
        }
        spawnEventoPreparado = null;
        jogoInicioTimeMillis = 0;
        relogQuitTime.clear();
        if (relogTask != null) {
            relogTask.cancel();
            relogTask = null;
        }
        if (contagemTask != null) {
            contagemTask.cancel();
            contagemTask = null;
        }
        state = EventState.AREA_BOLO;
        Bukkit.getScheduler().runTask(plugin, () -> {
            EventCosmeticsManager cx = plugin.getEventCosmeticsManager();
            if (cx != null) {
                cx.refreshEmEventoTodos();
            }
        });

        String nomeVencedor;
        if (vencedoresTime != null && !vencedoresTime.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (UUID u : vencedoresTime) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(Bukkit.getOfflinePlayer(u).getName());
            }
            String nomeTime = "Time";
            nomeVencedor = nomeTime + " (" + sb.toString() + ")";
        } else {
            nomeVencedor = vencedor != null ? Bukkit.getOfflinePlayer(vencedor).getName() : "Nenhum";
        }

        String msgFinal;
        if (vencedoresTime != null && !vencedoresTime.isEmpty()) {
            msgFinal = plugin.getVariablesManager().get("event.msg_time_ganhou", EventEmbeddedConfig.MSG_TIME_GANHOU)
                    .replace("%time%", "TIME");
        } else {
            msgFinal = plugin.getVariablesManager().get("event.msg_evento_finalizado", EventEmbeddedConfig.MSG_EVENTO_FINALIZADO)
                    .replace("%vencedor%", nomeVencedor);
        }
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msgFinal));

        if (vencedoresTime != null && !vencedoresTime.isEmpty()) {
            for (UUID u : vencedoresTime) {
                Player vw = Bukkit.getPlayer(u);
                if (vw != null && vw.isOnline()) {
                    celebrarVitóriaComFogos(vw.getLocation());
                    break;
                }
            }
        } else if (vencedor != null) {
            Player vw = Bukkit.getPlayer(vencedor);
            if (vw != null && vw.isOnline()) {
                celebrarVitóriaComFogos(vw.getLocation());
            }
        }

        plugin.getSchematicManager().carregarAreaBolo();
        Location bolo = plugin.getSchematicManager().getAreaBoloLocation();
        if (bolo != null && bolo.getWorld() != null) {
            bolo.getWorld().loadChunk(bolo.getBlockX() >> 4, bolo.getBlockZ() >> 4);
            bolo = bolo.clone().add(0, 1, 0);
        }
        final Location boloFinal = bolo;
        for (UUID uuid : participantes) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.setGameMode(GameMode.SURVIVAL);
                if (boloFinal != null) p.teleport(boloFinal);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getVariablesManager().get("event.msg_indo_bolo", EventEmbeddedConfig.MSG_INDO_BOLO))));
            }
        }
        for (UUID uuid : espectadores) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.setGameMode(GameMode.SURVIVAL);
                if (boloFinal != null) p.teleport(boloFinal);
            }
        }
        espectadores.clear();

        final String msgVitoria = msgFinal;
        final int tempoBolo = plugin.getVariablesManager().getInt("event.tempo_bolo", EventEmbeddedConfig.TEMPO_AREA_BOLO_SEGUNDOS);
        boloTask = new BukkitRunnable() {
            int t = tempoBolo;

            @Override
            public void run() {
                if (t <= 0) {
                    cancel();
                    enviarTodosParaLobby();
                    return;
                }
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msgVitoria));
                t--;
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void enviarTodosParaLobby() {
        state = EventState.ENVIANDO_LOBBY;
        if (boloTask != null) {
            boloTask.cancel();
            boloTask = null;
        }
        plugin.getBungeeManager().sinalizarReinicioServidor();
        String lobby = plugin.getVariablesManager().get("event.lobby_server", EventEmbeddedConfig.LOBBY_SERVER);
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getBungeeManager().enviarParaServidor(p, lobby);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            state = EventState.REINICIANDO;
            Bukkit.getScheduler().runTaskLater(plugin, Bukkit::shutdown, 40L);
        }, 60L);
    }

    public void pararEvento() {
        EventCosmeticsManager cos = plugin.getEventCosmeticsManager();
        if (cos != null) {
            cos.onEventoAdministrativamenteParado();
        }
        restaurarVelocidadeParticipantesAtuais();
        killsNaRodada.clear();
        if (eventoProntoAtual != null) {
            eventoProntoAtual.unregisterListeners();
            eventoProntoAtual.limparMapa();
            eventoProntoAtual = null;
        }
        spawnEventoPreparado = null;
        jogoInicioTimeMillis = 0;
        relogQuitTime.clear();
        if (relogTask != null) {
            relogTask.cancel();
            relogTask = null;
        }
        state = EventState.AGUARDANDO;
        if (contagemTask != null) {
            contagemTask.cancel();
            contagemTask = null;
        }
        if (countdownParaIniciarTask != null) {
            countdownParaIniciarTask.cancel();
            countdownParaIniciarTask = null;
        }
        if (boloTask != null) {
            boloTask.cancel();
            boloTask = null;
        }
        participantes.clear();
        vivos.clear();
        espectadores.clear();
        vencedor = null;
        vencedoresTime = null;
    }

    /** Vencedor solo em jogo já decidido (útil durante {@link EventState#AREA_BOLO}). */
    public UUID getUltimoVencedorSoloOpcional() {
        return vencedor;
    }

    /** Faz parte do time vencedor (modos em grupo). */
    public boolean isUuidNoTimeVencedor(UUID id) {
        return id != null && vencedoresTime != null && vencedoresTime.contains(id);
    }
}
