package com.minecraft.savagemc.core.api.punishment;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.punishment.objects.Revocation;
import com.minecraft.savagemc.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.savagemc.core.api.punishment.objects.enums.PunishmentReason;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.core.util.list.StringUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class Punishment {

    private final String id = "#" + StringUtil.generateLetterCode(4) + Savage.RANDOM.nextInt(9);

    private final UUID player;
    @Builder.Default
    private final UUID author = Constant.DEFAULT_ID;

    @Builder.Default
    private final String ip = "127.0.0.1";

    private final PunishmentCategory category;
    private final PunishmentReason reason;

    @Builder.Default
    private final String cause = "Não informado";

    @Builder.Default
    private final ServerType server = ServerType.BUNGEE;
    @Builder.Default
    private final ArcadeCategory arcade = ArcadeCategory.NONE;

    @Builder.Default
    private Revocation revocation = Revocation.builder().build();

    private final long createdAt = System.currentTimeMillis();

    @Builder.Default
    private final long expiresAt = -1L;

    @Builder.Default
    private boolean valid = true;

    protected void save(String... fields) {
        for (String field : fields) {
            Savage.getPunishmentData().update(this, field);
        }
    }

    public void revoke(UUID author, String reason) {
        this.revocation = Revocation.builder()
                .author(author)
                .reason(reason)
                .data(System.currentTimeMillis())
                .build();

        this.valid = false;

        Account account = Savage.getAccountData().of(player);

        if (account != null)
            account.updatePunishment(this);

        save("revocation", "valid");
    }

    public String getPlayerName() {
        Account account = Savage.getAccountData().of(player);

        return account != null ? account.getName() : "...";
    }

    public String getAuthorName() {
        Account account = Savage.getAccountData().of(author);

        return account != null ? account.getName() : "CONSOLE";
    }

    public boolean isTemporary() {
        return expiresAt > -1L;
    }

    public boolean hasExpired() {
        return isTemporary() && expiresAt <= System.currentTimeMillis();
    }
}
