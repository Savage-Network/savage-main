/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.util.leaderboard;

import com.minecraft.core.bukkit.util.leaderboard.Leaderboard;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@AllArgsConstructor
@Getter
@Setter
public class LeaderboardPreset {

    private Location location;
    private Leaderboard leaderboard;
    private String displayName;

    public Location getLocation() {
        return location.clone();
    }
}
