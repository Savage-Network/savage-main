package com.minecraft.savagemc.core.backend.database.redis.listener;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.backend.database.redis.RedisDatabase;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.logging.Level;

@Getter
public class RedisListener {

    private final RedisDatabase redis;
    private final JedisPubSub pubSub;

    private final String[] channels;

    public RedisListener(JedisPubSub pubSub, String... channels) {
        this.redis = Savage.getRedis();

        this.pubSub = pubSub;

        this.channels = channels;
    }

    public void load() {
        try (Jedis jedis = redis.getPool().getResource()) {
            try {
                jedis.subscribe(pubSub, channels);
            } catch (Exception e) {
                Savage.getLogger().log(Level.SEVERE, "Não foi possível registar os canais do Redis...", e);

                try {
                    pubSub.unsubscribe();
                } catch (Exception ignored) {
                }

                load();
            }
        }
    }
}
