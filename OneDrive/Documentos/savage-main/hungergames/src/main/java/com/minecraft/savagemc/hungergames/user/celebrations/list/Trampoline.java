package com.minecraft.savagemc.hungergames.user.celebrations.list;

import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.celebrations.Celebration;
import com.minecraft.savagemc.hungergames.user.celebrations.pattern.CelebrationRarity;
import com.minecraft.savagemc.hungergames.util.celebrations.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class Trampoline extends Celebration {

    private final Map<Block, MaterialData> trampoline = new HashMap<>();
    private Cuboid cuboid;
    private Location center;
    private boolean running;

    public Trampoline(HungerGames hungerGames) {
        super(hungerGames);
        setDisplayName("Trampolim");
        setDescription("Pule para sua vitória em seu trampolim.");
        setIcon(Pattern.of(Material.WOOL));
        setRarity(CelebrationRarity.RARE);
        setRank(Rank.VIP);
    }

    @Override
    public void onVictory(Player player) {
        Location loc1 = player.getLocation().add(-2, 0, -2);
        Location loc2 = player.getLocation().add(2, 15, 2);

        clearBlocks();

        center = player.getLocation();
        cuboid = new Cuboid(loc1, loc2);

        generateStructure();

        player.teleport(player.getLocation().add(0, 4, 0));

        running = true;

        new BukkitRunnable() {
            public void run() {
                if (running && cuboid != null) {
                    Bukkit.getScheduler().runTask(BukkitGame.getEngine(), () -> {
                        for (Entity entity : center.getWorld().getNearbyEntities(center, 4, 4, 4)) {
                            Block b = entity.getLocation().getBlock().getRelative(BlockFace.DOWN);
                            if (b.getType().toString().contains("WOOL") && cuboid.contains(b))
                                entity.setVelocity(new Vector(0, 3, 0));
                        }
                    });
                }
            }
        }.runTaskTimer(BukkitGame.getEngine(), 0, 5);
    }

    private void generateStructure() {
        genBarr(get(2, 0, 2));
        genBarr(get(-2, 0, 2));
        genBarr(get(2, 0, -2));
        genBarr(get(-2, 0, -2));

        genBlue(get(2, 1, 2));
        genBlue(get(2, 1, 1));
        genBlue(get(2, 1, 0));
        genBlue(get(2, 1, -1));
        genBlue(get(2, 1, -2));
        genBlue(get(-2, 1, 2));
        genBlue(get(-2, 1, 1));
        genBlue(get(-2, 1, 0));
        genBlue(get(-2, 1, -1));
        genBlue(get(-2, 1, -2));
        genBlue(get(1, 1, 2));
        genBlue(get(0, 1, 2));
        genBlue(get(-1, 1, 2));
        genBlue(get(1, 1, -2));
        genBlue(get(0, 1, -2));
        genBlue(get(-1, 1, -2));

        genBlack(get(0, 1, 0));
        genBlack(get(0, 1, 1));
        genBlack(get(1, 1, 0));
        genBlack(get(0, 1, -1));
        genBlack(get(-1, 1, 0));
        genBlack(get(1, 1, 1));
        genBlack(get(-1, 1, -1));
        genBlack(get(1, 1, -1));
        genBlack(get(-1, 1, 1));

        genLadder(get(-3, 1, 0));
        genLadder(get(-3, 0, 0));

        Bukkit.getScheduler().runTaskLater(getHungerGames(), this::clearBlocks, 240);
    }

    private void genBarr(Block block) {
        setToRestore(block, Material.FENCE, (byte) 0);
    }

    private void genBlue(Block block) {
        setToRestore(block, Material.valueOf("WOOL"), (byte) 11);
    }

    private void genBlack(Block block) {
        setToRestore(block, Material.valueOf("WOOL"), (byte) 15);
    }

    private void genLadder(Block block) {
        setToRestore(block, Material.LADDER, (byte) 4);
    }

    @SuppressWarnings("deprecation")
    private void setToRestore(Block block, Material material, byte data) {
        MaterialData materialData = new MaterialData(material, data);
        trampoline.put(block, materialData);
        block.setType(material);
        block.getState().setRawData(data);
        block.getState().update();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (cuboid != null && running && cuboid.contains(event.getBlock()))
            event.setCancelled(true);
        if (cuboid != null && running && (event.getBlock().getLocation().equals(center.getBlock().getRelative(-3, 0, 0).getLocation())
                || event.getBlock().getLocation().equals(center.getBlock().getRelative(-3, 1, 0).getLocation())))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (cuboid != null && running && cuboid.contains(event.getBlock()))
            event.setCancelled(true);
        if (cuboid != null && running && (event.getBlock().getLocation().equals(center.getBlock().getRelative(-3, 0, 0).getLocation())
                || event.getBlock().getLocation().equals(center.getBlock().getRelative(-3, 1, 0).getLocation())))
            event.setCancelled(true);
    }

    private void clearBlocks() {
        if (center != null) {
            get(-3, 0, 0).setType(Material.AIR);
            get(-3, 1, 0).setType(Material.AIR);
        }
        if (trampoline != null) {
            for (Block block : trampoline.keySet())
                block.setType(Material.AIR);
            trampoline.clear();
        }
        cuboid = null;
        running = false;
    }

    private Block get(int x, int y, int z) {
        return center.getBlock().getRelative(x, y, z);
    }

}
