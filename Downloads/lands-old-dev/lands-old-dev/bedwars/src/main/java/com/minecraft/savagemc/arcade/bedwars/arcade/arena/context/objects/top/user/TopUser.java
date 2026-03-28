package com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.top.user;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class TopUser {

    private final UUID id;
    private int value;

    public Account getAccount() {
        return Savage.getAccountData().of(id);
    }
}
