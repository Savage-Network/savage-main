package com.minecraft.savagemc.core.arcade.room.slot;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Slot {

    NONE("Nenhum", "0v0", 0, 0),
    SOLO("Solo", "1v1v1v1", 1, 8),      // 1 jogador por time, 8 times = 8 jogadores
    DUO("Duplas", "2v2v2v2", 2, 16),    // 2 jogadores por time, 8 times = 16 jogadores
    TRIO("Trio", "3v3v3v3", 3, 24),     // 3 jogadores por time, 8 times = 24 jogadores
    QUARTET("Quarteto", "4v4v4v4", 4, 32); // 4 jogadores por time, 8 times = 32 jogadores

    private final String name, id;
    private final int playersPerTeam;  // Jogadores por time
    private final int maxPlayers;      // Total de jogadores na partida

    public String getFullName() {
        return name + " (" + id + ")";
    }
}
