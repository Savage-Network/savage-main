package com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.top.type;

import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.top.Top;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.top.enums.TopType;
import com.minecraft.savagemc.arcade.bedwars.user.User;

public class FinalKillTop extends Top {

    public FinalKillTop() {
        super(TopType.FINAL_KILL);
    }

    @Override
    public int getStatistic(User user) {
        return user.getContext().getFinalKill();
    }
}
