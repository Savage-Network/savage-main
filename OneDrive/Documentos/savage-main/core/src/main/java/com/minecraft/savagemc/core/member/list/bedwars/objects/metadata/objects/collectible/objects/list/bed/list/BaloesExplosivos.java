package com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.list;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.BedDestroyCollectible;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.UUID;

public class BaloesExplosivos extends BedDestroyCollectible {

    private static final String SKULL_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGY4NGE0MzU1ZWU2MTBhMTJlOWRlMzEzMzg4ZDA2ZTZjN2VlZmU1YjFmOGQxMjU3YzIyNzExZjg0NzI0YzljZSJ9fX0=";

    public BaloesExplosivos() {
        super(CollectibleRarity.EPIC, "Balões Explosivos", Item.of(Material.FIREWORK), 1729792699944L);

        setLore("§7Balões que explodem no ar!");
        setRanks(RankType.SAVAGE);
    }

    @Override
    public void execute(Location location) {
        Location baseLocation = location.clone().add(0, 1, 0);
        Random rand = new Random();
        int balloonCount = 3;

        for (int i = 0; i < balloonCount; i++) {
            Location spawnLocation = baseLocation.clone().add(rand.nextDouble() - 0.5, 0, rand.nextDouble() - 0.5);
            ArmorStand skullEntity = spawnLocation.getWorld().spawn(spawnLocation, ArmorStand.class);
            skullEntity.setVisible(false);
            skullEntity.setCustomNameVisible(false);
            skullEntity.setGravity(false);
            ItemStack customSkull = getCustomSkull();
            skullEntity.getEquipment().setHelmet(customSkull);

            new BukkitRunnable() {
                int ticks = 0;
                
                @Override
                public void run() {
                    if (ticks >= 20) {
                        skullEntity.remove();
                        location.getWorld().createExplosion(skullEntity.getLocation().add(0, 5, 0), 0);
                        cancel();
                        return;
                    }
                    skullEntity.setVelocity(new Vector(0, 0.1, 0));
                    skullEntity.getWorld().playEffect(skullEntity.getLocation(), Effect.SMOKE, 0);
                    ticks++;
                }
            }.runTaskTimer(Savage.getJavaPlugin(), 1L, 2L);
        }
    }

    private ItemStack getCustomSkull() {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            setCustomSkull(meta, SKULL_TEXTURE);
            skull.setItemMeta(meta);
        }
        return skull;
    }

    private void setCustomSkull(SkullMeta meta, String texture) {
        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", texture));
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
