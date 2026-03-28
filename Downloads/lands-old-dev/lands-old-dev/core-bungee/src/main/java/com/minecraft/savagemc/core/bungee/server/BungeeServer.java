package com.minecraft.savagemc.core.bungee.server;

import com.minecraft.savagemc.core.backend.data.list.server.ServerData;
import com.minecraft.savagemc.core.server.Server;
import com.minecraft.savagemc.core.server.payload.ServerPayload;
import com.minecraft.savagemc.core.server.type.ServerType;

public class BungeeServer extends ServerData {

    @Override
    public int getPort() {
        return 25898;
    }

    @Override
    public void updatePlayerCounter(int players) {
        Server server = of(ServerType.BUNGEE);

        if (server != null) {
            ServerPayload payload = server.getPayload();

            payload.setHeartBeat(System.currentTimeMillis());
            payload.setPlayers(players);

            this.refresh(server);
        }
    }
}
