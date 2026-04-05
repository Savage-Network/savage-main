package com.minecraft.savagemc.core.backend.database.redis;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.backend.database.Database;
import com.minecraft.savagemc.core.backend.database.DatabaseCredentials;
import com.minecraft.savagemc.core.util.list.JsonUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Getter
@RequiredArgsConstructor
public class RedisDatabase implements Database {

    private final DatabaseCredentials credential;

    private JedisPool pool;

    public RedisDatabase(boolean local) {
        this(local
                // Local
                ? DatabaseCredentials.builder()
                .host("127.0.0.1")
                .port(6379)
                .build()
                // Server
                : DatabaseCredentials.builder()
                .host("206.0.29.85")
                .port(6379)
                .build());
    }

    @Override
    public void load() {
        Instant now = Instant.now();

        Savage.getLogger().info("Conectando ao Redis...");

        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                JedisPoolConfig config = new JedisPoolConfig();

                config.setMaxTotal(128);

                if (credential.getPassword() != null && !credential.getPassword().isEmpty())
                    pool = new JedisPool(config, credential.getHost(), credential.getPort(), 0, credential.getPassword());
                else
                    pool = new JedisPool(config, credential.getHost(), credential.getPort(), 0);

                break;
            } catch (Exception e) {
                Savage.getLogger().warning("Erro ao estabelecer conexão com o Redis. Tentativa " + (i + 1) + " de " + maxRetries);

                try {
                    Thread.sleep(1000); // Aguarda 1 segundo antes de tentar novamente
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (pool == null)
            Savage.getLogger().severe("Não foi possível efetuar a conexão com o Redis após " + maxRetries + " tentativas. Verifique as credenciais.");
        else
            Savage.getLogger().info("Conexão com Redis efetuada com sucesso. (Tempo médio: " + Duration.between(now, Instant.now()).toMillis() + "ms)");
    }

    @Override
    public void unload() {
        if (isAvailable()) {
            pool.destroy();
        }
    }

    @Override
    public boolean isAvailable() {
        return pool != null && !pool.isClosed();
    }

    public boolean exists(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.exists(key);
        }
    }

    public void save(String key, Object object) {
        try (Jedis jedis = pool.getResource()) {
            if (jedis.exists(key)) return;

            Map<String, String> fields = JsonUtil.objectToMap(object);

            fields.values().removeIf(Objects::isNull);

            jedis.hmset(key, fields);
        }
    }

    public void saveDefault(String key, Object value) {
        try (Jedis jedis = pool.getResource()) {
            if (jedis.exists(key)) return;

            jedis.set(key, value.toString());
        }
    }

    public void saveDefault(String key, Object value, int expires) {
        try (Jedis jedis = pool.getResource()) {
            if (jedis.exists(key)) return;

            jedis.setex(key, expires, value.toString());
        }
    }

    public Object getDefault(String key) {
        try (Jedis jedis = pool.getResource()) {
            if (!jedis.exists(key)) return null;

            return jedis.get(key);
        }
    }

    public void update(String key, Object object) {
        try (Jedis jedis = pool.getResource()) {
            if (!jedis.exists(key)) return;

            Map<String, String> fields = JsonUtil.objectToMap(object);

            fields.values().removeIf(Objects::isNull);

            jedis.hmset(key, fields);
        }
    }

    public void flush() {
        try (Jedis jedis = pool.getResource()) {
            jedis.flushAll();
        }
    }

    public void publish(String channel, String message) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, message);
        }
    }

    public void save(String key, Object object, int expire) {
        save(key, object);
        cache(key, expire);
    }

    public void delete(String key) {
        try (Jedis jedis = pool.getResource()) {
            jedis.del(key);
        }
    }

    public void cache(String key, int seconds) {
        try (Jedis jedis = pool.getResource()) {
            jedis.expire(key, seconds);
        }
    }

    public void persist(String key) {
        try (Jedis jedis = pool.getResource()) {
            jedis.persist(key);
        }
    }

    public int ttl(String key) {
        try (Jedis jedis = pool.getResource()) {
            return (int) jedis.ttl(key);
        }
    }

    public void delete(String key, String... fields) {
        try (Jedis jedis = pool.getResource()) {
            jedis.hdel(key, fields);
        }
    }

    public <T> T load(String key, Class<T> tClass) {
        if (!exists(key)) return null;

        T t;

        try (Jedis jedis = pool.getResource()) {
            Map<String, String> fields = jedis.hgetAll(key);

            if (fields == null || fields.isEmpty()) return null;

            fields.values().removeIf(Objects::isNull);

            t = JsonUtil.mapToObject(fields, tClass);
        }

        return t;
    }

    public <T> List<T> loadAll(String key, Class<T> type) {
        List<T> list = new ArrayList<>();

        try (Jedis jedis = pool.getResource()) {
            Set<String> keys = jedis.keys(key + "*");

            if (keys == null || keys.isEmpty()) return list;

            for (String keyName : keys) {
                Map<String, String> fields = jedis.hgetAll(keyName);

                if (fields == null || fields.isEmpty()) return list;

                fields.values().removeIf(Objects::isNull);

                T object = JsonUtil.mapToObject(fields, type);

                if (object != null)
                    list.add(type.cast(object));
            }
        }

        return list;
    }

    public void removeAll(String key) {
        try (Jedis jedis = pool.getResource()) {
            Set<String> keys = jedis.keys(key + "*");

            if (keys.isEmpty()) return;

            keys.forEach(jedis::del);
        }
    }

    public int getNumberKeys(String key) {
        int count;

        try (Jedis jedis = pool.getResource()) {
            Set<String> members = jedis.smembers(key);

            count = members.size();

            return count;
        }
    }
}
