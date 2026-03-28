package com.minecraft.savagemc.core.backend.database.redis.message.types.account;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.api.redirect.Redirect;
import com.minecraft.savagemc.core.backend.database.redis.message.RedisMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AccountVanishMessage extends RedisMessage {

    private final UUID id;

    private Redirect redirect;

    public AccountVanishMessage(UUID id) {
        super(Constant.REDIS_ACCOUNT_VANISH_CHANNEL);

        this.id = id;
    }
}
