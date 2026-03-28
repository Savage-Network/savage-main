package com.minecraft.savagemc.core.backend.database.redis.message.types.route.arcade;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

@Getter
public class ArenaPartyWarpMessage extends RedisMessage {

    private final Account receiver;
    private final ArcadeRouteContext route;

    public ArenaPartyWarpMessage(Account receiver, ArcadeRouteContext route) {
        super(Constant.REDIS_ARENA_PARTY_WARP_CHANNEL);

        this.receiver = receiver;
        this.route = route;
    }
}
