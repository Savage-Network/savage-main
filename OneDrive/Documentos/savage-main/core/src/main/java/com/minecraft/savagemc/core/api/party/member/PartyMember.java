package com.minecraft.savagemc.core.api.party.member;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class PartyMember {

    private final UUID id;

    private final long joinedAt = System.currentTimeMillis();

    private long expiresAt = -1L;

    public Account getAccount() {
        return Savage.getAccountData().of(id);
    }

    public boolean hasExpired() {
        return expiresAt > -1L && expiresAt <= System.currentTimeMillis();
    }
}
