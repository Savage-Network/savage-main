package com.minecraft.savagemc.core.backend.database.redis.message.types.account;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;

@Getter
public class AccountNickChangeMessage extends RedisMessage {

    private final Account account;
    private final String nickname;

    public AccountNickChangeMessage(Account account, String nickname) {
        super(Constant.REDIS_ACCOUNT_NICK_CHANGE_CHANNEL);

        this.account = account;
        this.nickname = nickname;
    }
}
