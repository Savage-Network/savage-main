package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.event.cooldown.CooldownFinishEvent;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.savagemc.hungergames.util.metadata.GameMetadata;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Spider extends Kit {

    protected final Map<UUID, Location[]> hashMap = new HashMap<>();

    public Spider(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.WEB));
        setItems(new ItemFactory(Material.WEB).setName("§aLança teia").setDescription("§7Kit Spider").getStack());
        setKitCategory(KitCategory.STRATEGY);
        setCooldown(15);
        setPrice(20000);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && immutableSet.contains(event.getAction()) && isUser(event.getPlayer()) && isItem(event.getItem())) {
            event.setCancelled(true);

            Player player = event.getPlayer();

            player.updateInventory();

            if (checkInvincibility(player))
                return;

            if (isCooldown(player)) {
                dispatchCooldown(player);
                return;
            }

            player.launchProjectile(Snowball.class).setMetadata("kit.spider", new GameMetadata(true));

            addCooldown(player.getUniqueId());
            player.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Snowball && isPlayer(event.getEntity())) {
            if (!event.getDamager().hasMetadata("kit.spider"))
                return;
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSnowballHit(ProjectileHitEvent event) {
        final Projectile projectile = event.getEntity();

        if (!(projectile instanceof Snowball))
            return;

        if (!(projectile.getShooter() instanceof Player))
            return;

        final Player player = (Player) projectile.getShooter();

        if (!isUser(player))
            return;

        if (!projectile.hasMetadata("kit.spider"))
            return;

        Location[] allLocations = getLocations(projectile.getLocation());

        this.hashMap.put(player.getUniqueId(), allLocations);

        for (Location locations : allLocations) {
            final Block block = locations.getBlock();

            if (materialImmutableSet.contains(block.getType())) {
                block.setType(Material.WEB);
            }
        }

        player.sendMessage("§aVocê lançou uma teia.");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCooldownExpireEvent(CooldownFinishEvent event) {
        if (event.getCooldown().getKey().equalsIgnoreCase("kit.cooldown.spider")) {
            final Player player = event.getPlayer();

            for (Location locations : this.hashMap.get(player.getUniqueId())) {
                if (locations.getBlock().getType() == Material.WEB)
                    locations.getBlock().setType(Material.AIR);
            }

            this.hashMap.remove(event.getPlayer().getUniqueId());

            player.playSound(player.getLocation(), Sound.SPIDER_DEATH, 1F, 1F);
        }
    }

    protected Location[] getLocations(final Location location) {
        return new Location[]{location.clone().add(0, 0, 0), location.clone().add(0, 0, 1), location.clone().add(0, 0, 0), location.clone().add(1, 0, 1), location.clone().add(0, 0, 0), location.clone().add(0, 0, 1), location.clone().add(1, 0, 0), location.clone().add(1, 0, 0), location.clone().add(0, 0, 0)};
    }

    protected final ImmutableSet<Material> materialImmutableSet = Sets.immutableEnumSet(Material.AIR, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM, Material.DOUBLE_PLANT, Material.DEAD_BUSH, Material.GRASS, Material.LONG_GRASS, Material.YELLOW_FLOWER, Material.RED_ROSE, Material.SEEDS, Material.MELON_SEEDS, Material.PUMPKIN_SEEDS);
    protected final ImmutableSet<Action> immutableSet = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

}
