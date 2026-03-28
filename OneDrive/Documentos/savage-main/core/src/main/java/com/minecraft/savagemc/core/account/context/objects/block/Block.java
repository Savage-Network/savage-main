package com.minecraft.savagemc.core.account.context.objects.block;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Block {

    private final UUID id;

    private final long timestamp = System.currentTimeMillis();

    public String getName() {
        Account account = Savage.getAccountData().of(id);

        return account != null ? account.getNickname() : "...";
    }
}
