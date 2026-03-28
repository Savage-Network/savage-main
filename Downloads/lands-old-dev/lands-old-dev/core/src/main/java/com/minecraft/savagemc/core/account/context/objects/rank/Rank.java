package com.minecraft.savagemc.core.account.context.objects.rank;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.assignment.Assignment;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.account.context.objects.rank.type.role.RankRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
public class Rank {

    @Builder.Default
    private final RankType type = RankType.MEMBER;

    @Builder.Default
    private final Assignment assignment = Assignment.AUTO;
    @Builder.Default
    private final UUID author = Constant.DEFAULT_ID;

    private final long assignedAt = System.currentTimeMillis();

    @Builder.Default
    private final long expiresAt = -1L;

    public String getName() {
        return getType() != null ? getType().getName() : RankType.MEMBER.getName();
    }

    public String getColoredName() {
        return getType() != null ? getType().getColoredName() : RankType.MEMBER.getColoredName();
    }

    public String getAuthorName() {
        Account account = Savage.getAccountData().of(author, false);

        return account != null ? account.getName() : author.equals(Constant.DEFAULT_ID) ? "CONSOLE" : "...";
    }

    public String getColor() {
        RankType t = getType() != null ? getType() : RankType.MEMBER;
        return t.getColor().toString() + (t.ordinal() > RankType.MIDIA_PLUS.ordinal() ? "§o" : "");
    }

    public boolean isPermanent() {
        return expiresAt <= -1L;
    }

    public boolean isPayment() {
        return getType() != null && getType().getRole().equals(RankRole.VIP);
    }

    public boolean hasExpired() {
        return !isPermanent() && expiresAt <= System.currentTimeMillis();
    }
}
