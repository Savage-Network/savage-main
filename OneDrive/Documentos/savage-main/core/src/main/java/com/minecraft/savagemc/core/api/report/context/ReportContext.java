package com.minecraft.savagemc.core.api.report.context;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ReportContext {

    private final UUID sender;
    private final String reason;
    private final String replayId; // ID do replay da partida

    private final long timestamp = System.currentTimeMillis(), expiresAt;

    public Account getAccount() {
        return Savage.getAccountData().of(sender);
    }

    public boolean isValid() {
        return expiresAt > System.currentTimeMillis();
    }
}
