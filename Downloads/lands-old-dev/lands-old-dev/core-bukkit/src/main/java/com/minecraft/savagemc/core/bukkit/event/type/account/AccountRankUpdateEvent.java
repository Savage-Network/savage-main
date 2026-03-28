package com.minecraft.savagemc.core.bukkit.event.type.account;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.Rank;
import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountRankUpdateEvent extends EventHandler {
    private final Account account;
    private final Rank rank;
}
