package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Pyro extends Kit {

    public Pyro(HungerGames hungerGames) {
        super(hungerGames);
        setKitCategory(KitCategory.STRATEGY);
        setIcon(Pattern.of(Material.FIREBALL));
        setPrice(20000);
        setCooldown(15);
        setItems(new ItemFactory(Material.FIREBALL).setName("§aFogo").setDescription("§7Kit Pyro").getStack());
        setPermission(Rank.VIP);
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

        if (isCooldown(player)) {
            dispatchCooldown(player);
            return;
        }

        Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setVelocity(fireball.getVelocity().clone().multiply(0.9));
        fireball.setIsIncendiary(true);
        fireball.setYield(fireball.getYield() * 0.8F);
        player.updateInventory();
        addCooldown(player.getUniqueId());
    }


    @Override
    public void grant(Player player) {
        super.grant(player);
        player.getInventory().addItem(new ItemStack(Material.FLINT_AND_STEEL));
    }
}
