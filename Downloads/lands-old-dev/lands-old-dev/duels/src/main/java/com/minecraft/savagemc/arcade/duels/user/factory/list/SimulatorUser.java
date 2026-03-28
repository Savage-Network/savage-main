package com.minecraft.savagemc.arcade.duels.user.factory.list;

import com.minecraft.savagemc.core.arcade.route.join.Join;
import com.minecraft.savagemc.core.member.list.duels.DuelMember;
import com.minecraft.savagemc.arcade.duels.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.duels.arcade.list.combat.simulator.kit.model.Kit;
import com.minecraft.savagemc.arcade.duels.user.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimulatorUser extends User {

    private Kit kit = null;

    public SimulatorUser(DuelMember member, Arena arena, Join join) {
        super(member, arena, join);
    }

    public boolean isUsingKit(Kit kit) {
        return this.kit != null && this.kit.getName().equalsIgnoreCase(kit.getName());
    }
}
