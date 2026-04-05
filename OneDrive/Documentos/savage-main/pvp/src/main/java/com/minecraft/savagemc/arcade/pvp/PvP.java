package com.minecraft.savagemc.arcade.pvp;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.option.ServerOptions;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.savagemc.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.savagemc.core.server.type.ServerType;

public class PvP extends BukkitCore {

    @Override
    public void onLoad() {
        super.onLoad();

        /* Configurações do Servidor */
        ServerOptions.DAMAGE_ENABLED = true;
        ServerOptions.DROP_ITEM_ENABLED = true;

        ServerOptions.DEFAULT_CHAT = false;

        saveDefaultConfig();

        Savage.setServerType(ServerType.PVP);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        /* Iniciando Comandos/Eventos */
        new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".arcade.pvp");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".arcade.pvp");

        getManager().getArcade().handle(this, Constant.SOURCE_DIR + ".arcade.pvp.arcade.list");

        Savage.getLogger().info("PvP iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        Savage.getLogger().info("PvP encerrado com sucesso.");
    }
}
