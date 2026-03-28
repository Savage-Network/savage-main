package com.minecraft.savagemc.arcade.bedwars.structure.team.objects.upgrade.structure.trap.action;

import com.minecraft.savagemc.arcade.bedwars.structure.team.Team;
import org.bukkit.entity.Player;

public interface TrapAction {
    void execute(Player executor, Team team);
}
