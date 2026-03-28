package com.minecraft.savagemc.core.account.context.objects.friend;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.friend.toggle.FriendToggle;
import com.minecraft.savagemc.core.account.context.objects.friend.type.FriendType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Friend {

    private final UUID id;

    private FriendType type;
    private FriendToggle toggle;

    private final long startedAt;

    public Friend(Account account) {
        this.id = account.getId();

        this.type = FriendType.DEFAULT;
        this.toggle = new FriendToggle();

        this.startedAt = System.currentTimeMillis();
    }

    public boolean isValid() {
        return Savage.getAccountData().of(id) != null;
    }

    public String getName() {
        Account account = Savage.getAccountData().of(id);

        return isValid() ? account.getNickname() : "...";
    }
}
