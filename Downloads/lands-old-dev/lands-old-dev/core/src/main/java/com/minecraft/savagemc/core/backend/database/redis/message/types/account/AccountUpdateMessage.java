package com.minecraft.savagemc.core.backend.database.redis.message.types.account;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.backend.database.redis.message.RedisMessage;
import com.google.gson.JsonElement;
import lombok.Getter;

@Getter
public class AccountUpdateMessage extends RedisMessage {

    private final Account account;

    private final String field;
    private final JsonElement value;

    public AccountUpdateMessage(Account account, String field, JsonElement value) {
        super(Constant.REDIS_ACCOUNT_UPDATE_CHANNEL);

        this.account = account;

        this.field = field;
        this.value = value;
    }
}
