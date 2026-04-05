package com.minecraft.game;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.savagemc.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.savagemc.core.server.type.ServerType;
import lombok.Getter;

import java.util.ArrayList;

public class Game extends BukkitCore {

    @Getter
    private static Game instance;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;

        Savage.getLogger().info("Iniciando Game Events...");

        saveDefaultConfig();

        Savage.setServerType(ServerType.EVENT);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Savage.getServerData().local().setWhitelist(true, new ArrayList<>(), new ArrayList<>());

        new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".game.command");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".game");

        getManager().getArcade().handle(this, Constant.SOURCE_DIR + ".game.arcade.list");

        Savage.getLogger().info("Game Events iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        Savage.getLogger().info("Game Events desligado com sucesso.");
    }
}
