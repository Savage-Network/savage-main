package com.minecraft.savagemc.arcade.aquatic.listener;

import com.minecraft.savagemc.arcade.aquatic.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.aquatic.user.User;
import com.minecraft.savagemc.core.arcade.room.map.area.Cuboid;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.savagemc.core.bukkit.manager.list.CooldownManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil.consumeItem;
import static com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil.removeItemInHand;

public class ItemListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || user.isProtected()) return;

        if (event.hasItem()) {
            ItemStack item = event.getItem();

            if (item == null || item.isType(Material.AIR)) return;

            Arena arena = user.getArena();

            if (arena.isPhase(RoomPhase.PLAYING) && event.getAction().name().contains("RIGHT")) {
                switch (item.getType()) {
                    case FIREBALL: {
                        if (hasCooldown(player, "Fireball")) return;

                        consumeItem(player);

                        Fireball fireball = player.launchProjectile(Fireball.class);

                        fireball.getWorld().playSound(fireball.getLocation(), Sound.GHAST_FIREBALL, 1, 1);

                        fireball.setYield(0);
                        fireball.setFireTicks(0);
                        fireball.setIsIncendiary(false);

                        setCooldown(player, "Fireball", 1);
                        break;
                    }

                    case MAGMA_CREAM: {
                        if (hasCooldown(player, "Estilingue")) return;

                        consumeItem(player);

                        Vector vector = player.getEyeLocation().getDirection().multiply(1.1F).setY(1.2F);

                        player.setFallDistance(-1.0F);
                        player.setVelocity(vector);

                        player.playSound(Sound.NOTE_PIANO);

                        setCooldown(player, "Estilingue", 5);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        ItemStack item = event.getItem();

        if (item == null || item.isType(Material.AIR)) return;

        int id = item.getDurability();

        // Consumiu a poção de força
        if (item.isType(Material.POTION) && id == 8201) {
            event.setCancelled(true);

            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 15, 0, false, false));

            player.sendMessage("§eVocê recebeu §bForça I§e por §b15§e segundos!");

            removeItemInHand(player);
        }
    }

    @SuppressWarnings("unchecked")
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        if (projectile instanceof Fireball) {

            if (event.getHitEntity() instanceof Fireball)
                return;

            World world = projectile.getWorld();

            Location location = projectile.getLocation();

            int yield = 3;

            world.createExplosion(location.getX(), location.getY(), location.getZ(), yield, false, false);

            List<Block> blocks = new ArrayList<>(Cuboid.getBlocksFromCenter(projectile, yield));

            blocks.removeIf(block -> block.getType().isOre());

            EntityExplodeEvent entityExplodeEvent = new EntityExplodeEvent(projectile, location, blocks, yield);

            Bukkit.getPluginManager().callEvent(entityExplodeEvent);
        }
    }

    @EventHandler
    public void onEntityExplodes(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        Location location = event.getLocation();

        World world = location.getWorld();

        Arena arena = (Arena) BukkitCore.getManager().getArcade().getArenas().stream()
                .filter(room -> room.getWorld().equals(world)).findFirst().orElse(null);

        event.setCancelled(true);

        if (arena != null) {
            for (Player player : arena.getPlayers()) {
                if (player.getLocation().distance(location) <= 5) {
                    Vector direction = player.getLocation().toVector().subtract(location.toVector());

                    if (direction.length() > 0)
                        direction.normalize();
                    else
                        direction = new Vector(0, 0, 1);

                    double horizontal = entity instanceof Fireball ? 3.2 : 0;

                    Vector horizontalVelocity = direction.multiply(horizontal);

                    double vertical = entity instanceof Fireball ? 1.080 : 0;

                    horizontalVelocity.setY(vertical);

                    player.setVelocity(horizontalVelocity);
                }
            }
        }
    }

    protected boolean hasCooldown(Player player, String name) {
        CooldownManager manager = BukkitCore.getManager().getCooldown();

        if (manager.hasCooldown(player, "item-" + name)) {
            Cooldown cooldown = manager.getCooldown(player, "item-" + name);

            if (cooldown == null) return false;

            player.sendMessage("§cAguarde " + new DecimalFormat("#.#").format(cooldown.getRemaining()) + " para usar " + name + " novamente.");
            return true;
        }

        return false;
    }

    protected void setCooldown(Player player, String name, long time) {
        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Cooldown cooldown = new Cooldown("item-" + name, time);

        BukkitCore.getManager().getCooldown().addCooldown(player, cooldown);
    }
}
