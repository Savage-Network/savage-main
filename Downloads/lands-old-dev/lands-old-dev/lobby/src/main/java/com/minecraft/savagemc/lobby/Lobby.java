package com.minecraft.savagemc.lobby;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.option.ServerOptions;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.savagemc.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.savagemc.lobby.architect.loader.ArchitectLoader;
import lombok.Getter;
import org.bukkit.Bukkit;

public class Lobby extends BukkitCore {

    @Getter
    private static ArchitectLoader loader;

    @Override
    public void onLoad() {
        super.onLoad();

        loader = new ArchitectLoader(this);

        Savage.Logger().info("Iniciando Lobby...");

        Savage.setServerType(loader.getArchitect().getServer());

        ServerOptions.SPAWN_CREATURES = true;
        ServerOptions.DEFAULT_CHAT = false;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (loader.getArchitect() == null) {
            Savage.Logger().severe("Não foi possível carregar as configurações...");

            Bukkit.shutdown();
        } else
            Bukkit.getScheduler().runTask(this, () -> loader.getArchitect().load());

        // Registrar collectibles
        Savage.getCollectibleController().handle(this, "com.minecraft.savagemc.core.api.collectible.type");

        new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".lobby.command");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".lobby");

        Savage.Logger().info("Lobby iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        loader.getArchitect().unload();

        Savage.Logger().info("Lobby encerrado com sucesso.");
    }
}
