package com.minecraft.savagemc.core.backend.database.redis.message.types.account;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

@Getter
public class AccountClanChangeMessage extends RedisMessage {

    private final Account account;

    public AccountClanChangeMessage(Account account) {
        super(Constant.REDIS_ACCOUNT_CLAN_CHANGE_CHANNEL);

        this.account = account;
    }
}
