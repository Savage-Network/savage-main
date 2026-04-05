package com.minecraft.savagemc.core.api.report.replay;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.Location;

import java.util.UUID;

@Getter
@Builder
public class ReplayFrame {

    private final UUID player;
    private final Location location;
    private final float yaw;
    private final float pitch;
    private final String action; // MOVE, ATTACK, BREAK_BLOCK, PLACE_BLOCK, etc
    private final long timestamp;
}
