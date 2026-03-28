package com.minecraft.savagemc.arcade.aquatic.arcade.team;

import com.minecraft.savagemc.arcade.aquatic.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.aquatic.user.User;
import com.minecraft.savagemc.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.savagemc.core.arcade.room.team.preset.TeamPresetType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class Team extends TeamPreset {

    private int remainingLives = 3;

    public Team(TeamPresetType type, Arena arena, Location base, int maxPlayers) {
        super(type, arena, base, maxPlayers);
    }

    public Arena getArena() {
        return (Arena) super.getArena();
    }

    public List<User> getAliveUsers() {
        return getArena().getAliveUsers()
                .stream()
                .filter(user -> user.getTeam() != null && user.getTeam().equals(this))
                .collect(Collectors.toList());
    }

    public boolean isDead() {
        if (getAliveUsers().isEmpty()) return true;

        return remainingLives <= 0;
    }

    public Player getFirstMember() {
        return getPlayers().stream().findFirst().orElse(null);
    }

    public String getAliveDisplay() {
        return isDead() ? "§c✘" : "§a" + remainingLives;
    }
}
