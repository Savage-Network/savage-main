package com.minecraft.savagemc.core.backend.data.list.user.arcade.list;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.backend.data.list.user.arcade.MemberData;
import com.minecraft.savagemc.core.backend.database.postgres.PostgresDatabase;
import com.minecraft.savagemc.core.backend.database.redis.RedisDatabase;
import com.minecraft.savagemc.core.member.list.bedwars.BedMember;

public class BedWarsData extends MemberData<BedMember> {

    public BedWarsData(PostgresDatabase postgres, RedisDatabase redis) {
        super(postgres, redis, Constant.MONGODB_MEMBER_BEDWARS_COLLECTION);
    }
}
