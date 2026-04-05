package com.minecraft.savagemc.core.bukkit.event.type.account;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class AccountVanishLogEvent extends EventHandler {

    private final Account account;
    private final VanishState state;

    public AccountVanishLogEvent(Account account, VanishState state) {
        this.account = account;
        this.state = state;
    }

    public enum VanishState {
        JOIN, LEAVE
    }

    public Account getAccount() {
        return account;
    }

    public VanishState getState() {
        return state;
    }

    public boolean isState(VanishState state) {
        return this.state.equals(state);
    }
}
