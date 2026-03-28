package com.minecraft.savagemc.core.member.context.menu;

import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuMetadata {

    private final ArcadeCategory arcade;
    private final String base64Snapshot;

    private String base64;
    private long lastUpdate;

    public MenuMetadata(ArcadeCategory arcade, String base64) {
        this.arcade = arcade;
        this.base64Snapshot = base64;

        this.base64 = base64;
        this.lastUpdate = System.currentTimeMillis();
    }
}
