package com.minecraft.savagemc.core.bungee.event.list.account;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bungee.event.EventHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;

@Getter
@RequiredArgsConstructor
public class AccountRefreshEvent extends EventHandler {

    private final Account account;
    private final Field field;

    private final Object old, value;
}
