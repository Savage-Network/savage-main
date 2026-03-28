package com.minecraft.savagemc.core.backend.data.list.user.arcade.list;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.backend.data.list.user.arcade.MemberData;
import com.minecraft.savagemc.core.backend.database.postgres.PostgresDatabase;
import com.minecraft.savagemc.core.backend.database.redis.RedisDatabase;
import com.minecraft.savagemc.core.member.list.pvp.PvPMember;

public class PvPData extends MemberData<PvPMember> {

    public PvPData(PostgresDatabase postgres, RedisDatabase redis) {
        super(postgres, redis, Constant.MONGODB_MEMBER_PVP_COLLECTION);
    }
}
