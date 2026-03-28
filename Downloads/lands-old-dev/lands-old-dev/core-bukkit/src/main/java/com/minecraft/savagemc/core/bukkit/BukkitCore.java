package com.minecraft.savagemc.core.bukkit;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.backend.database.redis.listener.RedisListener;
import com.minecraft.savagemc.core.bukkit.api.permission.regex.RegexPermissions;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.savagemc.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.savagemc.core.bukkit.manager.Manager;
import com.minecraft.savagemc.core.bukkit.manager.list.TaskManager;
import com.minecraft.savagemc.core.bukkit.server.BukkitServer;
import com.minecraft.savagemc.core.bukkit.service.filter.BukkitLogFilter;
import com.minecraft.savagemc.core.bukkit.service.packet.PacketHook;
import com.minecraft.savagemc.core.bukkit.service.redis.BukkitRedis;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConstant;

import java.util.concurrent.CompletableFuture;

public class BukkitCore extends JavaPlugin {

    @Getter
    private static BukkitCore instance;

    @Getter
    private static Manager manager;

    @Getter
    private RegexPermissions permissions;

    private final String[] COMMANDS_TO_DISABLE = new String[]{
            "minecraft:",
            "icanhasbukkit", "?", "about", "help", "ban", "ban-ip", "banlist", "clear", "deop",
            "op", "difficulty", "effect", "enchant", "kick", "list", "me", "say", "gamerule",
            "scoreboard", "seed", "spawnpoint", "spreadplayers", "summon", "tell", "tellraw", "testfor",
            "testforblocks", "weather", "xp", "reload", "rl", "worldborder", "achievement",
            "blockdata", "clone", "defaultgamemode", "entitydata", "execute", "fill", "gamemode",
            "pardon", "pardon-ip", "replaceitem", "setidletimeout", "testforblock", "title",
            "trigger", "viaver", "ps", "holograms", "hd", "holo", "hologram", "restart", "filter",
            "packetlog", "?", "minecraft:gamerule", "minecraft:gm", "minecraft:gr",
            "minecraft:kill", "minecraft:pl", "minecraft:plugin", "minecraft:plugins", "minecraft:save-all",
            "give", "minecraft:give", "minecraft:list", "minecraft:save-off", "minecraft:save-on", "minecraft:setblock", "minecraft:setworldspawn",
            "minecraft:time", "ver", "version", "minecraft:stop",
            "minecraft:ver", "minecraft:version", "minecraft:playsound", "playsound", "particle", "minecraft:particle",
            "packet", "packet_filter", "pl", "plugin", "plugins", "list", "minecraft:list",
            "protocol", "protocollib:packet", "protocollib:packet_filter", "protocollib:protocol",
            "viaversion:viaversion", "viaversion", "viaversion:vvbukkit", "vvbukkit", "save-on", "save-off", "setblock",
            "setworldspawn", "kill", "minecraft:tp", "swm"
    };

    @Override
    public void onLoad() {
        Savage.Logger().info("Iniciando Bukkit...");

        instance = this;
        Savage.setJavaPlugin(this);

        new BukkitLogFilter().registerFilter();

        /* Configurando Spigot Customizado */
        SpigotConstant.SERVER_TITLE = Constant.SERVER_TITLE;
        SpigotConstant.SERVER_SUPPORT_URL = Constant.SERVER_DISCORD;

        Savage.load(new BukkitPlatform(this), new BukkitServer(), Savage.Logger());
    }

    @Override
    public void onEnable() {
        manager = new Manager();

        if (Savage.getServerData() == null) {
            Savage.Logger().severe("ServerData é null! O onLoad() falhou. Verifique os logs de inicialização.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Savage.Logger().info("[DEBUG] Iniciando ServerData...");
        Savage.getServerData().start(Bukkit.getMaxPlayers());

        Savage.Logger().info("[DEBUG] Iniciando RedisListener...");
        CompletableFuture.runAsync(() -> new RedisListener(new BukkitRedis(), Constant.REDIS_CHANNELS).load());

        Savage.Logger().info("[DEBUG] Removendo comandos...");
        Savage.getPlatform().runSync(() -> BukkitUtil.removeCommand(this, COMMANDS_TO_DISABLE), 3L);

        Savage.Logger().info("[DEBUG] Registrando comandos...");
        new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".core.bukkit.command");

        Savage.Logger().info("[DEBUG] Registrando listeners...");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".core.bukkit");

        Savage.Logger().info("[DEBUG] Registrando tasks...");
        TaskManager.handle(this, Constant.SOURCE_DIR + ".core.bukkit.service.task.list");

        Savage.Logger().info("[DEBUG] Registrando PacketHook...");
        new PacketHook(this).handle();

        this.permissions = new RegexPermissions();

        Savage.Logger().info("Bukkit iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        if (this.permissions != null)
            this.permissions.onDisable();

        // Redirecionar os jogadores para o Lobby
        if (!Savage.getServerType().equals(ServerType.HUB))
            Savage.getAccountController().list().forEach(Account::redirectToHub);

        if (Savage.getServerType().isArcade())
            manager.getArcade().unload();

        TaskManager.unloadTasks();

        Savage.unload();
    }
}
