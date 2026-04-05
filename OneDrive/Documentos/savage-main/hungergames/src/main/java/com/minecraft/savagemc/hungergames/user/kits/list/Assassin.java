package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class Assassin extends Kit {

    public Assassin(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.GOLD_SWORD));
        setKitCategory(KitCategory.COMBAT);
        setItems(new ItemFactory().setSkullURL("f9878853654c3bb26fd33e80f8ed3cdf03ab3247f73678464e06da143dbd0c17").setAmount(1).setName("§aMurder").setDescription("§7Kit Assassin").getStack());
        setCooldown(15);
    }

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (!ACCEPTABLES_INTERACT.contains(event.getAction()))
            return;

        final Player player = event.getPlayer();

        if (!isUser(player))
            return;

        if (!isItem(player.getItemInHand()))
            return;

        if (isCooldown(player) || isCombat(player)) {
            dispatchCooldown(player);
            return;
        }

        event.setCancelled(true);

        Location location = player.getTargetBlock((Set<Material>) null, 7).getLocation();

        location.setYaw(player.getLocation().getYaw());
        location.setPitch(player.getLocation().getPitch());

        player.teleport(location);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0), true);

        addCooldown(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(BlockPlaceEvent event) {

        Player player = event.getPlayer();

        ItemStack itemStack = player.getItemInHand();

        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;

        if (isUser(player) && isItem(itemStack)) {

            event.setCancelled(true);
            player.updateInventory();

            if (checkInvincibility(player))
                return;

            if (isCombat(player) || isCooldown(player)) {
                dispatchCooldown(player);
                return;
            }

            Location location = player.getTargetBlock((Set<Material>) null, 7).getLocation();

            location.setYaw(player.getLocation().getYaw());
            location.setPitch(player.getLocation().getPitch());

            player.teleport(location);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0), true);

            addCooldown(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player && event.getEntity() instanceof Player))
            return;
        final Player damager = (Player) event.getDamager();

        if (isUser(damager) && damager.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            damager.removePotionEffect(PotionEffectType.INVISIBILITY);

            event.setDamage(event.getDamage() + 2.0D);
        }
    }

}
