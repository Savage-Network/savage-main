package com.minecraft.arcade.eventos;

import com.minecraft.arcade.eventos.commands.EspectadorCommand;
import com.minecraft.arcade.eventos.commands.EventoCommand;
import com.minecraft.arcade.eventos.commands.EventoTabCompleter;
import com.minecraft.arcade.eventos.commands.EvtreloadCommand;
import com.minecraft.arcade.eventos.commands.GrupoCommand;
import com.minecraft.arcade.eventos.commands.GrupoTabCompleter;
import com.minecraft.arcade.eventos.commands.LmCommand;
import com.minecraft.arcade.eventos.commands.LmTabCompleter;
import com.minecraft.arcade.eventos.commands.StartCommand;
import com.minecraft.arcade.eventos.commands.StartTabCompleter;
import com.minecraft.arcade.eventos.commands.TempoCommand;
import com.minecraft.arcade.eventos.commands.TempoTabCompleter;
import com.minecraft.arcade.eventos.commands.TimeCommand;
import com.minecraft.arcade.eventos.commands.TimeTabCompleter;
import com.minecraft.arcade.eventos.commands.VarCommand;
import com.minecraft.arcade.eventos.commands.VarTabCompleter;
import com.minecraft.arcade.eventos.eventos.EventoRegistry;
import com.minecraft.arcade.eventos.listeners.SpectatorMenuListener;
import com.minecraft.arcade.eventos.managers.EventManager;
import com.minecraft.arcade.eventos.managers.EventCosmeticsManager;
import com.minecraft.arcade.eventos.managers.EventMedalStore;
import com.minecraft.arcade.eventos.managers.EventState;
import com.minecraft.arcade.eventos.managers.EventoTipo;
import com.minecraft.arcade.eventos.managers.EventosPredefinidosManager;
import com.minecraft.arcade.eventos.managers.VariablesManager;
import com.minecraft.arcade.eventos.managers.BungeeManager;
import com.minecraft.arcade.eventos.managers.ClanEventManager;
import com.minecraft.arcade.eventos.managers.SchematicManager;
import com.minecraft.arcade.eventos.scoreboard.EventScoreboard;
import com.minecraft.arcade.eventos.settings.EventEmbeddedConfig;
import com.minecraft.arcade.eventos.utils.EventUiTheme;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.option.ServerOptions;
import com.minecraft.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;

public final class Eventos extends BukkitCore {

    private static Eventos plugin;

    private EventManager eventManager;
    private SchematicManager schematicManager;
    private BungeeManager bungeeManager;
    private ClanEventManager clanEventManager;
    private EventScoreboard eventScoreboard;
    private VariablesManager variablesManager;
    private EventMedalStore eventMedalStore;
    private EventCosmeticsManager eventCosmeticsManager;
    private EventosPredefinidosManager eventosPredefinidosManager;
    private EventoRegistry eventoRegistry;
    private SpectatorMenuListener spectatorMenuListener;

    public static Eventos getPlugin() {
        return plugin;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        plugin = this;

        getLogger().info("Iniciando Eventos...");
        saveDefaultConfig();

        ServerOptions.DEFAULT_CHAT = false;
        ServerOptions.DAMAGE_ENABLED = true;
        ServerOptions.FOOD_ENABLED = false;
        ServerOptions.DROP_ITEM_ENABLED = true;
        ServerOptions.SPAWN_CREATURES = false;

        Core.setServerType(ServerType.EVENTOS);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        criarPastas();
        garantirMundoPlano();

        schematicManager = new SchematicManager(this);
        bungeeManager = new BungeeManager(this);
        clanEventManager = new ClanEventManager(this);
        eventManager = new EventManager(this);
        eventScoreboard = new EventScoreboard(this);
        variablesManager = new VariablesManager(this);
        variablesManager.carregar();
        eventMedalStore = new EventMedalStore(this);
        eventMedalStore.ensureFile();
        eventCosmeticsManager = new EventCosmeticsManager(this, eventMedalStore);
        aplicarVariaveisAoEstadoDoEvento();

        eventosPredefinidosManager = new EventosPredefinidosManager();
        eventosPredefinidosManager.carregar();
        eventoRegistry = new EventoRegistry(this);

        getCommand("lm").setExecutor(new LmCommand(this));
        getCommand("lm").setTabCompleter(new LmTabCompleter(this));
        getCommand("start").setExecutor(new StartCommand(this));
        getCommand("start").setTabCompleter(new StartTabCompleter(this));
        getCommand("time").setExecutor(new TimeCommand(this));
        getCommand("time").setTabCompleter(new TimeTabCompleter());
        getCommand("tempo").setExecutor(new TempoCommand(this));
        getCommand("tempo").setTabCompleter(new TempoTabCompleter());
        getCommand("evtreload").setExecutor(new EvtreloadCommand(this));
        getCommand("grupo").setExecutor(new GrupoCommand(this));
        getCommand("grupo").setTabCompleter(new GrupoTabCompleter());
        getCommand("var").setExecutor(new VarCommand(this));
        getCommand("var").setTabCompleter(new VarTabCompleter());
        getCommand("evento").setExecutor(new EventoCommand(this));
        getCommand("evento").setTabCompleter(new EventoTabCompleter());
        getCommand("espectador").setExecutor(new EspectadorCommand(this));

        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".arcade.eventos.listeners");
        spectatorMenuListener = new SpectatorMenuListener();
        getServer().getPluginManager().registerEvents(spectatorMenuListener, this);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (eventScoreboard != null) {
                eventScoreboard.atualizar();
            }
            if (eventManager != null) {
                eventManager.sincronizarIngressoComServidorEVanish();
            }
        }, 40L, 40L);

        getLogger().info(ChatColor.stripColor(EventUiTheme.CHAT_PREFIX.trim()) + " Plugin habilitado. Comando principal: /lm");
    }

    @Override
    public void onDisable() {
        if (eventManager != null) {
            eventManager.pararEvento();
        }
        super.onDisable();
        getLogger().info("Eventos desabilitado.");
    }

    /**
     * Sincroniza {@link EventManager} com {@link VariablesManager} (nome e tipo vindos das variáveis embutidas / {@code /var}).
     */
    public void aplicarVariaveisAoEstadoDoEvento() {
        if (eventManager == null || variablesManager == null) {
            return;
        }
        String nome = variablesManager.get("event.name", EventEmbeddedConfig.EVENTO_NOME_PADRAO);
        eventManager.setNomeEventoAtual(nome);
        EventoTipo tipo = EventoTipo.fromString(
                variablesManager.get("event.persist_tipo", EventEmbeddedConfig.EVENTO_TIPO_INICIAL.name()));
        eventManager.setEventoTipo(tipo);
    }

    /**
     * Mantém {@link EventManager} alinhado quando um admin altera chaves críticas pelo {@code /var}.
     */
    public void aplicarVariavelAdministrativa(CommandSender sender, String chave, String valor) {
        if (eventManager == null || variablesManager == null) return;
        switch (chave) {
            case "event.name":
                if (valor != null && !valor.isEmpty()) {
                    eventManager.setNomeEventoAtual(valor.trim());
                }
                break;
            case "event.persist_tipo":
                if (eventManager.getState() != EventState.AGUARDANDO) {
                    sender.sendMessage(ChatColor.YELLOW + "Valor salvo nas variáveis. O tipo só é aplicado ao EventManager quando não há evento ativo (use /lm parar se precisar).");
                    break;
                }
                EventoTipo t = EventoTipo.parseTipo(valor);
                if (t == null) {
                    sender.sendMessage(ChatColor.RED + "Tipo inválido em event.persist_tipo. Use solo, duplas, trio ou panela.");
                    break;
                }
                eventManager.setEventoTipo(t);
                variablesManager.set("event.persist_tipo", t.name());
                if (t.isEmTime()) {
                    variablesManager.set("event.max_por_clan", String.valueOf(t.getTamanhoTime()));
                    sender.sendMessage(ChatColor.GRAY + "Limite por grupo ajustado para " + t.getTamanhoTime() + " jogadores.");
                } else {
                    variablesManager.set("event.max_por_clan", String.valueOf(EventEmbeddedConfig.MAX_JOGADORES_POR_CLAN));
                }
                sender.sendMessage(ChatColor.GREEN + "Tipo do evento aplicado: " + t.name());
                break;
            default:
                break;
        }
    }

    private void criarPastas() {
        java.io.File dir = new java.io.File(getDataFolder(), EventEmbeddedConfig.PASTA_SCHEMATICS);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Garante que existe um mundo plano ao iniciar. Se o mundo não estiver carregado, cria como superflat.
     * Para o servidor usar apenas este mundo, defina em server.properties: level-name=world e level-type=FLAT.
     */
    private void garantirMundoPlano() {
        if (!EventEmbeddedConfig.MUNDO_PLANO_ATIVO) return;
        String nomeMundo = EventEmbeddedConfig.MUNDO_PLANO_NOME;
        if (nomeMundo == null || nomeMundo.isEmpty()) return;
        World w = Bukkit.getWorld(nomeMundo);
        if (w == null) {
            WorldCreator wc = WorldCreator.name(nomeMundo).type(WorldType.FLAT);
            w = Bukkit.createWorld(wc);
            if (w != null) {
                getLogger().info("Mundo plano criado: " + nomeMundo);
            }
        }
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public SchematicManager getSchematicManager() {
        return schematicManager;
    }

    public BungeeManager getBungeeManager() {
        return bungeeManager;
    }

    public ClanEventManager getClanEventManager() {
        return clanEventManager;
    }

    public EventScoreboard getEventScoreboard() {
        return eventScoreboard;
    }

    public VariablesManager getVariablesManager() {
        return variablesManager;
    }

    public EventosPredefinidosManager getEventosPredefinidosManager() {
        return eventosPredefinidosManager;
    }

    public EventoRegistry getEventoRegistry() {
        return eventoRegistry;
    }

    public SpectatorMenuListener getSpectatorMenuListener() {
        return spectatorMenuListener;
    }

    public EventMedalStore getEventMedalStore() {
        return eventMedalStore;
    }

    public EventCosmeticsManager getEventCosmeticsManager() {
        return eventCosmeticsManager;
    }
}
