package com.minecraft.savagemc.auth;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.option.ServerOptions;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.savagemc.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.savagemc.core.server.type.ServerType;

public class Auth extends BukkitCore {

    @Override
    public void onLoad() {
        super.onLoad();

        Savage.Logger().info("Iniciando Auth...");

        Savage.setServerType(ServerType.AUTH);

        ServerOptions.DEFAULT_CHAT = false;
        ServerOptions.CHAT_ENABLED = false;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".auth.command");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".auth");

        Savage.Logger().info("Auth iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        Savage.Logger().info("Auth desligado com sucesso.");
    }
}
