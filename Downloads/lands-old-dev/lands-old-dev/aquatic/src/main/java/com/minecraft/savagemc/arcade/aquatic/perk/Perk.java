package com.minecraft.savagemc.arcade.aquatic.perk;

import com.minecraft.savagemc.arcade.aquatic.user.User;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.savagemc.core.bukkit.manager.list.CooldownManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Perk {

    private final String name;
    private final Item icon;

    private final RankType rank;

    private final List<String> lore = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();

    private final int price;

    private final long cooldown;

    public Perk(String name, Item icon) {
        this(name, icon, RankType.MEMBER, 0, -1L);
    }

    public void handle(Player player) {
    }

    public void setLore(String... lore) {
        this.lore.addAll(Arrays.asList(lore));
    }

    public void setItems(Item... items) {
        this.items.addAll(Arrays.asList(items));
    }

    public boolean has(Account account) {
        if (isFree()) return true;

        return account.hasPermission(getPermission()) || account.hasRank(rank);
    }

    public boolean isUsing(UUID id) {
        User user = (User) User.of(id);

        return user != null && user.isUsingPerk(name) && !user.isProtected() && user.getArena().isPhase(RoomPhase.PLAYING);
    }

    public boolean isFree() {
        return rank.equals(RankType.MEMBER) || price <= 0;
    }

    public String getPermission() {
        return "aquatic.perk." + name.toLowerCase();
    }

    public boolean hasCooldown(Player player) {
        return hasCooldown(player, true);
    }

    public boolean hasCooldown(Player player, boolean withMessage) {
        CooldownManager manager = BukkitCore.getManager().getCooldown();

        String name = getName().toLowerCase();

        if (manager.hasCooldown(player, "perk-" + name)) {
            Cooldown cooldown = manager.getCooldown(player, "perk-" + name);

            if (cooldown == null) return false;

            if (withMessage)
                player.sendMessage("§cAguarde " + new DecimalFormat("#.#").format(cooldown.getRemaining()) + "s para usar a habilidade " + getName() + " novamente.");

            return true;
        }

        return false;
    }

    public void applyCooldown(Player player) {
        BukkitCore.getManager().getCooldown().addCooldown(player.getUniqueId(), "perk-" + name, getCooldown());
    }
}
