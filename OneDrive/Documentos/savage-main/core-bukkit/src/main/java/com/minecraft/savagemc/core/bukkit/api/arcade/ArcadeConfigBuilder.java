package com.minecraft.savagemc.core.bukkit.api.arcade;

import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.room.map.Map;

import java.util.List;
import java.util.UUID;

public class ArcadeConfigBuilder {
    private UUID creator;
    private ArcadeCategory arcade;
    private Integer slot;
    private List<Map> maps;
    private java.util.Map<String, Object> properties;

    public ArcadeConfigBuilder setCreator(UUID creator) {
        this.creator = creator;
        return this;
    }

    public ArcadeConfigBuilder setArcade(ArcadeCategory arcade) {
        this.arcade = arcade;
        return this;
    }

    public ArcadeConfigBuilder setSlot(Integer slot) {
        this.slot = slot;
        return this;
    }

    public ArcadeConfigBuilder setMaps(List<Map> maps) {
        this.maps = maps;
        return this;
    }

    public ArcadeConfigBuilder setProperties(java.util.Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public ArcadeConfig build() {
        return ArcadeConfig.builder()
                .creator(creator)
                .arcade(arcade)
                .slot(slot)
                .build();
    }
}
