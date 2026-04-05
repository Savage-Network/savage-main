package com.minecraft.savagemc.arcade.bedwars.listener;

import com.minecraft.savagemc.arcade.bedwars.user.User;
import org.bukkit.event.Listener;

import java.util.UUID;

public class ReportReplayListener implements Listener {

    // Método estático para obter o replayId de um jogador
    public static String getPlayerReplayId(UUID playerId) {
        User user = (User) User.of(playerId);
        if (user != null) {
            return user.getCurrentReplayId();
        }
        return null;
    }
}
