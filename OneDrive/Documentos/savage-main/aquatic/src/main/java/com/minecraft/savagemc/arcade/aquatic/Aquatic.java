package com.minecraft.savagemc.arcade.aquatic;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.savagemc.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.savagemc.core.server.type.ServerType;

import java.util.ArrayList;

public class Aquatic extends BukkitCore {

    @Override
    public void onLoad() {
        super.onLoad();

        Savage.Logger().info("Iniciando Water Fight...");

        saveDefaultConfig();

        Savage.setServerType(ServerType.AQUATIC);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Savage.getServerData().local().setWhitelist(true, new ArrayList<>(), new ArrayList<>());

        new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".arcade.aquatic.command");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".arcade.aquatic");

        getManager().getArcade().handle(this, Constant.SOURCE_DIR + ".arcade.aquatic.arcade.list");

        Savage.Logger().info("Water Fight iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        Savage.Logger().info("Water Fight desligado com sucesso.");
    }
}
