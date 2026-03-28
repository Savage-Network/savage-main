package com.minecraft.savagemc.core.api.collectible.type.artifact;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.collectible.Collectible;
import com.minecraft.savagemc.core.api.collectible.CollectibleCategory;
import com.minecraft.savagemc.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.savagemc.core.api.collectible.type.artifact.listener.ArtifactListener;
import com.minecraft.savagemc.core.api.item.Item;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
public abstract class ArtifactCollectible extends Collectible implements Listener {

    public ArtifactCollectible(String name, CollectibleRarity rarity, List<RankType> ranks, long releasedAt) {
        super(name, CollectibleCategory.ARTIFACT, rarity, ranks, releasedAt);

        Bukkit.getPluginManager().registerEvents(this, Savage.getJavaPlugin());

        Savage.getCollectibleController().save(this);
    }

    public abstract void handle(Player host);

    public ItemStack getArtifactItem() {
        if (getIcon() == null) return null;

        return Item.of(getIcon().getType(), "§a" + getName() + " §7(Clique)");
    }

    public String getCooldownKey() {
        return "artifact-collectible-cooldown-" + getName().toLowerCase();
    }

    public boolean hasCooldown(Player player) {
        Account account = Savage.getAccountController().of(player.getUniqueId());

        if (account == null) return false;

        if (account.hasCooldown(getCooldownKey())) {
            account.send("§cAguarde " + account.getFormattedCooldown(getCooldownKey()) + " para usar " + getName() + " novamente.");
            return true;
        }

        return false;
    }

    public void setCooldown(Player player, long time) {
        Account account = Savage.getAccountController().of(player.getUniqueId());

        if (account == null) return;

        if (!account.isStaffer())
            account.setCooldown(getCooldownKey(), time);
    }

    public static void handle() {
        Bukkit.getPluginManager().registerEvents(new ArtifactListener(), Savage.getJavaPlugin());
    }
}
