package com.minecraft.savagemc.core.bukkit.event.type.account;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import lombok.Getter;

@Getter
public class AccountClanChangeEvent extends EventHandler {
    protected Account account;

    public AccountClanChangeEvent() {
        this.account = null;
    }

    public AccountClanChangeEvent(Account account) {
        this.account = account;
    }
}
