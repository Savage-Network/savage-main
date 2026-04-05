package com.minecraft.savagemc.core.bukkit.event.type.account;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import lombok.Getter;

@Getter
public class AccountRankUpdateEvent extends EventHandler {
    protected final Account account;
    protected final RankType rank;

    public AccountRankUpdateEvent() {
        this.account = null;
        this.rank = null;
    }

    public AccountRankUpdateEvent(Account account, RankType rank) {
        this.account = account;
        this.rank = rank;
    }
}
