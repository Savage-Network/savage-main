package com.minecraft.savagemc.core.backend.data.list.api;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.arcade.ArcadeHolder;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.payload.ArcadePayload;
import com.minecraft.savagemc.core.arcade.room.payload.RoomPayload;
import com.minecraft.savagemc.core.arcade.room.type.Type;
import com.minecraft.savagemc.core.backend.database.redis.RedisDatabase;
import com.minecraft.savagemc.core.server.type.ServerType;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ArcadeData {

    private final RedisDatabase redis;

    private final String ARCADE_KEY = "arcade:";

    public void save(ArcadeHolder arcade) {
        List<RoomPayload> properties = new ArrayList<>();

        arcade.getRooms().forEach(room -> properties.add(new RoomPayload(room)));

        redis.save(ARCADE_KEY + arcade.getCategory().getServer().name().toLowerCase() + "-" + Savage.getServerId() + ":" + arcade.getCategory().name().toLowerCase(),
                new ArcadePayload(Savage.getServerId(), arcade.getCategory(), arcade.getMaps(), properties));
    }

    public synchronized List<ArcadePayload> getPayloads() {
        return new ArrayList<>(redis.loadAll(ARCADE_KEY, ArcadePayload.class));
    }

    public ArcadePayload read(ArcadeCategory category) {
        return read(category, Savage.getServerId());
    }

    public ArcadePayload read(ArcadeCategory category, int serverId) {
        return redis.load(ARCADE_KEY + category.getServer().name().toLowerCase() + "-" + serverId + ":" + category.name().toLowerCase(), ArcadePayload.class);
    }

    public int getOnlinePlayers(ServerType server) {
        if (!server.isArcade()) return 0;

        List<ArcadeCategory> categories = ArcadeCategory.of(server);

        int sum = 0;

        for (ArcadeCategory category : categories)
            sum += getOnlinePlayers(category);

        return sum;
    }

    public int getOnlinePlayers(ArcadeCategory category) {
        int onlinePlayers = 0;

        for (ArcadePayload payload : getPayloads()) {
            if (payload.getCategory().equals(category))
                onlinePlayers += payload.getOnlinePlayers();
        }

        return onlinePlayers;
    }

    public void delete(ArcadeCategory category) {
        redis.delete(ARCADE_KEY + category.getServer().name().toLowerCase() + "-" + Savage.getServerId() + ":" + category.name().toLowerCase());
    }

    public void delete(ServerType server) {
        getPayloads().stream()
                .filter(payload -> payload.getCategory().getServer().equals(server))
                .forEach(payload -> redis.delete(ARCADE_KEY + server.name().toLowerCase() + "-" + Savage.getServerId() + ":" + payload.getCategory().name().toLowerCase()));
    }

    public void update(ArcadePayload property) {
        redis.update(ARCADE_KEY + property.getCategory().getServer().name().toLowerCase() + "-" + Savage.getServerId() + ":" + property.getCategory().name().toLowerCase(), property);
    }
}
