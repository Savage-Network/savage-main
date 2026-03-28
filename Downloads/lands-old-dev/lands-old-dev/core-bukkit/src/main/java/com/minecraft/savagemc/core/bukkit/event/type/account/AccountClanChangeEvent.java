package com.minecraft.savagemc.core.bukkit.event.type.account;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountClanChangeEvent extends EventHandler {
    private final Account account;
}
