package com.minecraft.savagemc.arcade.bedwars.event;

import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.structure.team.Team;
import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
public class PlayerBedBreakEvent extends EventHandler {

    private final Player player;

    private final Arena arena;
    private final Team team, bedTeam;

    private final Location location;
}
