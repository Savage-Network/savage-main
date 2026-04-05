package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Jumper extends Kit {

    public Jumper(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.ENDER_PEARL));
        setCooldown(10);
        setKitCategory(KitCategory.MOVEMENT);
        setPrice(25000);
        setCombatCooldown(true);
        setItems(new ItemFactory(Material.ENDER_PEARL).setName("§aLançar").setDescription("§7Kit Jumper").getStack());
    }

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!ACCEPTABLES_INTERACT.contains(event.getAction()))
            return;

        if (!isUser(player))
            return;

        if (!isItem(player.getItemInHand()))
            return;

        event.setCancelled(true);

        if (isCombat(player) || isCooldown(player)) {
            dispatchCooldown(player);
            return;
        }

        player.setFallDistance(0);
        EnderPearl enderPearl = player.launchProjectile(EnderPearl.class);
        enderPearl.setPassenger(player);
        addCooldown(player.getUniqueId());
        Account.fetch(event.getPlayer().getUniqueId()).setProperty("hg.kit.launcher.no_fall_damage", true);
        player.updateInventory();
    }

    @Variable(name = "hg.kit.jumper.combat_cooldown_time", permission = Rank.ADMINISTRATOR)
    public double duration = 4;

    @Override
    public double getCombatTime() {
        return duration;
    }

}
