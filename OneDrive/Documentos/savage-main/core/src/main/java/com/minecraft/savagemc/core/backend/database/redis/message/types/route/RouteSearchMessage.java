package com.minecraft.savagemc.core.backend.database.redis.message.types.route;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.route.RouteContext;
import com.minecraft.savagemc.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

@Getter
public class RouteSearchMessage extends RedisMessage {

    private final Account account;
    private final RouteContext route;

    public RouteSearchMessage(Account account, RouteContext route) {
        super(Constant.REDIS_ROUTE_SEARCH_CHANNEL);

        this.account = account;
        this.route = route;
    }
}
