package com.minecraft.savagemc.core.api.report.replay;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReplayRecorder {

    private static final Map<String, Replay.ReplayBuilder> activeRecordings = new HashMap<>();
    private static final Map<String, Replay> completedReplays = new HashMap<>();

    public static void startRecording(String arenaId, String gameMode, String arenaName) {
        Replay.ReplayBuilder builder = Replay.builder()
                .id(arenaId + "_" + System.currentTimeMillis())
                .gameMode(gameMode)
                .arenaName(arenaName)
                .startTime(System.currentTimeMillis());
        
        activeRecordings.put(arenaId, builder);
    }

    public static void recordFrame(String arenaId, Player player, String action) {
        Replay.ReplayBuilder builder = activeRecordings.get(arenaId);
        if (builder == null) return;

        ReplayFrame frame = ReplayFrame.builder()
                .player(player.getUniqueId())
                .location(player.getLocation())
                .yaw(player.getLocation().getYaw())
                .pitch(player.getLocation().getPitch())
                .action(action)
                .timestamp(System.currentTimeMillis())
                .build();

        // Adiciona frame ao replay em construção
        // Nota: Precisamos armazenar frames temporariamente
    }

    public static String stopRecording(String arenaId, UUID reportedPlayer) {
        Replay.ReplayBuilder builder = activeRecordings.remove(arenaId);
        if (builder == null) return null;

        Replay replay = builder
                .reportedPlayer(reportedPlayer)
                .endTime(System.currentTimeMillis())
                .build();

        completedReplays.put(replay.getId(), replay);
        
        return replay.getId();
    }

    public static Replay getReplay(String replayId) {
        return completedReplays.get(replayId);
    }

    public static void deleteReplay(String replayId) {
        completedReplays.remove(replayId);
    }
}
