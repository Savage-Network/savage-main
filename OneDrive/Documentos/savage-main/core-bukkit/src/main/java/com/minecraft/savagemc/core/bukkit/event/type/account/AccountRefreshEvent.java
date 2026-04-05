package com.minecraft.savagemc.core.bukkit.event.type.account;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import lombok.Getter;

import java.lang.reflect.Field;

@Getter
public class AccountRefreshEvent extends EventHandler {

    private final Account account;
    private final Field field;
    private final Object oldValue;
    private final Object newValue;

    public AccountRefreshEvent(Account account, Field field, Object oldValue, Object newValue) {
        this.account = account;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    public Account getAccount() {
        return account;
    }

    public Field getField() {
        return field;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }
}
