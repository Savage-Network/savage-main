package com.minecraft.savagemc.core.bungee;

import com.minecraft.savagemc.core.bungee.bot.Bot;
import com.minecraft.savagemc.core.bungee.bot.command.structure.manager.SlashCommandManager;
import com.minecraft.savagemc.core.bungee.server.BungeeServer;
import com.minecraft.savagemc.core.bungee.service.security.AntiDDoSService;
import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.backend.database.redis.listener.RedisListener;
import com.minecraft.savagemc.core.bungee.command.structure.BungeeCommandHandler;
import com.minecraft.savagemc.core.bungee.listener.handler.ListenerHandler;
import com.minecraft.savagemc.core.bungee.service.filter.BungeeLogFilter;
import com.minecraft.savagemc.core.bungee.service.redis.BungeeRedis;
import com.minecraft.savagemc.core.bungee.service.task.UpdateTask;
import com.minecraft.savagemc.core.server.Server;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class BungeeCore extends Plugin {

    @Getter
    private static BungeeCore instance;

    public static BungeeCore getInstance() {
        return instance;
    }

    @Getter
    private Bot bot;

    @Override
    public void onLoad() {
        getLogger().info("Iniciando Bungee...");

        instance = this;

        new BungeeLogFilter().registerFilter();

        Savage.load(new BungeePlatform(this), new BungeeServer(), getLogger());

        Savage.getPartyData().clear();
        Savage.getAccountData().clearCache();
    }

    @Override
    public void onEnable() {
        Savage.getServerData().start(2024);

        new BungeeCommandHandler(this).handle(Constant.SOURCE_DIR + ".core.bungee.command");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".core.bungee");
        getProxy().getPluginManager().registerListener(this, new AntiDDoSService());

        Savage.getPlatform().runAsync(() -> new RedisListener(new BungeeRedis(), Constant.REDIS_CHANNELS).load());

        getProxy().getScheduler().schedule(this, new UpdateTask(), 0, 1, TimeUnit.SECONDS);

        this.bot = new Bot();

        SlashCommandManager.handle(Constant.SOURCE_DIR + ".core.bungee.bot.command.list");

        getLogger().info("Bungee iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        if (bot != null)
            bot.disconnect();

        Savage.getSkinCacheData().clear();

        Savage.unload();
    }
}
