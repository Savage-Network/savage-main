package com.minecraft.savagemc.arcade.bedwars.user.context;

import com.minecraft.savagemc.arcade.bedwars.user.context.objects.tracker.UserTracker;
import com.minecraft.savagemc.core.api.reconnect.Reconnect;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class UserContext {

    private UserTracker tracker = new UserTracker();

    private BedWarsItem pickaxeItem, axeItem, armorItem;
    private Reconnect reconnect;

    private int xpGain, kills, deaths, finalKill, bedDestruction;

    private boolean purchasedShears;

    private long milkTime = -1L, protectionTime = -1L;

    public boolean hasReconnect() {
        return reconnect != null;
    }

    public void downGradePickaxe(Player player) {
        if (pickaxeItem == null || pickaxeItem.equals(BedWarsItem.TOOLS_WOOD_PICKAXE)) return;

        BedWarsItem previousPick = BedWarsItem.of(item -> item.getStack().getType().name().endsWith("_PICKAXE")
                && item.ordinal() == (pickaxeItem.ordinal() - 1));

        BukkitUtil.replaceItemByType(player, pickaxeItem.getStack().getType(), previousPick.getStack());

        this.pickaxeItem = previousPick;
    }

    public void downGradeAxe(Player player) {
        if (axeItem == null || axeItem.equals(BedWarsItem.TOOLS_WOOD_AXE)) return;

        BedWarsItem previousAxe = BedWarsItem.of(item -> item.getStack().getType().name().endsWith("_AXE")
                && item.ordinal() == (axeItem.ordinal() - 1));

        BukkitUtil.replaceItemByType(player, axeItem.getStack().getType(), previousAxe.getStack());

        this.axeItem = previousAxe;
    }
}
