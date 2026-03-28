package com.minecraft.savagemc.core.api.redirect;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.route.RouteContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Redirect {

    private final UUID sender, target;
    private final RouteContext route;

    private final long createdAt = System.currentTimeMillis();

    public String getTargetName() {
        Account target = Savage.getAccountData().of(this.target);

        return target != null ? target.getNickname() : "Ninguém";
    }
}
