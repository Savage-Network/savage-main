package com.minecraft.savagemc.core.backend.database.redis.message;

import com.minecraft.savagemc.core.Savage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RedisMessage {

    private final String channel;

    public void send() {
        if (Savage.getRedis() != null) {
            Savage.getRedis().publish(channel, Savage.GSON.toJson(this));
            System.out.println("[RedisMessage] Mensagem enviada para o canal: " + channel);
        } else {
            System.err.println("[RedisMessage] Redis não está conectado! Mensagem não enviada para: " + channel);
        }
    }
}
