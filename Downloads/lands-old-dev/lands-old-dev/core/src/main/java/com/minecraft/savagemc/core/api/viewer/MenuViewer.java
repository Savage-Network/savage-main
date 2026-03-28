package com.minecraft.savagemc.core.api.viewer;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.util.list.StringUtil;
import lombok.Getter;

@Getter
public class MenuViewer {

    private static final String REDIS_KEY = "menu-viewer:";

    private final String id, nickName, base64, mode;

    private final double finalLife;
    private final long endedAt;

    public MenuViewer(String nickName, String base64, String mode, double finalLife) {
        this.id = StringUtil.generateLetterCode(5) + Savage.RANDOM.nextInt(5);
        this.nickName = nickName;

        this.base64 = base64;
        this.mode = mode;

        this.finalLife = finalLife;
        this.endedAt = System.currentTimeMillis();

        Savage.getRedis().save(REDIS_KEY + id, this, 60 * 2);
    }

    public static MenuViewer of(String id) {
        return Savage.getRedis().load(REDIS_KEY + id, MenuViewer.class);
    }
}
