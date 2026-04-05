package com.minecraft.savagemc.core.bukkit.event.type.account;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.tag.prefix.TagPrefix;
import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class AccountPrefixTypeEvent extends EventHandler {

    private final Account account;
    private final TagPrefix prefix;

    public AccountPrefixTypeEvent(Account account, TagPrefix prefix) {
        this.account = account;
        this.prefix = prefix;
    }

    public Account getAccount() {
        return account;
    }

    public TagPrefix getPrefix() {
        return prefix;
    }
}
