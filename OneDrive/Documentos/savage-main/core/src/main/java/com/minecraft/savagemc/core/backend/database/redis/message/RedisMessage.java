package com.minecraft.savagemc.core.backend.database.redis.message;

import com.minecraft.savagemc.core.Savage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RedisMessage {

    private final String channel;

    public void send() {
        Savage.getRedis().publish(channel, Savage.GSON.toJson(this));
    }
}
