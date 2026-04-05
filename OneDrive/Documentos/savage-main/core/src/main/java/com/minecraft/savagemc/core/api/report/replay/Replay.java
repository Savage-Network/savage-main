package com.minecraft.savagemc.core.api.report.replay;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class Replay {

    private final String id;
    private final UUID reportedPlayer;
    private final String gameMode;
    private final String arenaName;
    
    @Builder.Default
    private final List<ReplayFrame> frames = new ArrayList<>();
    
    private final long startTime;
    private final long endTime;
    
    public void addFrame(ReplayFrame frame) {
        frames.add(frame);
    }
    
    public int getDuration() {
        return (int) ((endTime - startTime) / 1000);
    }
}
