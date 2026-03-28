package com.minecraft.savagemc.core.bukkit.server;

import com.minecraft.savagemc.core.backend.data.list.server.ServerData;
import org.bukkit.Bukkit;

public class BukkitServer extends ServerData {

    @Override
    public int getPort() {
        return Bukkit.getPort();
    }
}
