package com.minecraft.savagemc.core.bukkit.user;

import com.minecraft.savagemc.core.account.Account;
import org.bukkit.entity.Player;

public class User {
    private final Player player;
    private final Account account;

    public User(Player player, Account account) {
        this.player = player;
        this.account = account;
    }

    public Player getPlayer() {
        return player;
    }

    public Account getAccount() {
        return account;
    }
}
