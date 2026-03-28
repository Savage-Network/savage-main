package com.minecraft.savagemc.core.bukkit.event.type.server;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class ServerRedisMessageEvent extends EventHandler {

    private final String channel;
    private final String message;

    private final JsonObject json;

    public ServerRedisMessageEvent(String channel, String message) {
        this.channel = channel;
        this.message = message;

        this.json = Savage.PARSER.parse(message).getAsJsonObject();
    }
}
