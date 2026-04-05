package com.minecraft.savagemc.hungergames.user.kits.list;

import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SandMan extends Kit {

    protected final List<Integer> integers = new ArrayList<>();

    public SandMan(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.SAND));
        setKitCategory(KitCategory.COMBAT);
        setPrice(25000);
        setReleasedAt(1757262059322L);
        setPermission(Rank.Savage);

        this.integers.add(250);
        this.integers.add(280);
        this.integers.add(300);
        this.integers.add(350);
        this.integers.add(390);
        this.integers.add(-250);
        this.integers.add(-280);
        this.integers.add(-300);
        this.integers.add(-350);
        this.integers.add(-390);
    }

    @EventHandler
    public void onServerHeartbeat(ServerHeartbeatEvent event) {
        if (!event.isPeriodic(20))
            return;
        getPlugin().getUserStorage().getUsers().forEach(this::apply);
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (event.getBlock().getType() == Material.CACTUS) {
            if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
                if (isUser(player))
                    Bukkit.getPluginManager().callEvent(new BlockBreakEvent(event.getBlock(), player));
            }
        }
    }

    public void apply(User user) {
        if (!user.isAlive() || !isUser(user))
            return;

        Player player = user.getPlayer();

        if (player.getLocation().getBlock().getBiome() == Biome.DESERT) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 118, 1), true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 118, 0), true);
        }
    }

    @Override
    public void grant(Player player) {
        super.grant(player);

        List<Integer> coordinates = new ArrayList<>(this.integers);
        Collections.shuffle(coordinates);

        Location location = getWorld().getSpawnLocation();

        for (int x : coordinates) {
            for (int z : coordinates) {
                if (getWorld().getHighestBlockAt(x, z).getBiome() == Biome.DESERT) {
                    if (!getWorld().getHighestBlockAt(x, z).getChunk().isLoaded())
                        getWorld().getHighestBlockAt(x, z).getChunk().load();
                    location = new Location(getWorld(), x, getWorld().getHighestBlockYAt(x, z), z);
                    break;
                }
            }
        }

        player.teleport(location);
    }

}
