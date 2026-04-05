package com.minecraft.savagemc.core.backend.database.redis.message.types.report;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ReportReplayMessage extends RedisMessage {
    
    private final UUID reportedPlayer;
    private final String replayId;

    public ReportReplayMessage(UUID reportedPlayer, String replayId) {
        super(Constant.REDIS_REPORT_CHANNEL);
        
        this.reportedPlayer = reportedPlayer;
        this.replayId = replayId;
    }
}
