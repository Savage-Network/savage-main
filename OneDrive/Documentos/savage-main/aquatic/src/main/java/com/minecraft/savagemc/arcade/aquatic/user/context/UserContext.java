package com.minecraft.savagemc.arcade.aquatic.user.context;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserContext {

    private int xpGain, kills, deaths, finalKill;

    private int respawnTime;
    private long protectionTime = -1L;
}
