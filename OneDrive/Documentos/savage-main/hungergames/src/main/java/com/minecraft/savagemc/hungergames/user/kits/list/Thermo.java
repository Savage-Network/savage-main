package com.minecraft.savagemc.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class Thermo extends Kit {

    public Thermo(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.DAYLIGHT_DETECTOR));
        setKitCategory(KitCategory.STRATEGY);
        setPrice(25000);
        setCooldown(30);
        setItems(new ItemFactory(Material.DAYLIGHT_DETECTOR).setName("§aSunlight sensor").getStack());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && event.getAction() == Action.RIGHT_CLICK_BLOCK && isItem(event.getItem())) {

            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);

            final Player player = event.getPlayer();

            if (isUser(player)) {
                player.updateInventory();

                if (checkInvincibility(player))
                    return;

                if (isCooldown(player)) {
                    dispatchCooldown(player);
                    return;
                }

                Block block = event.getClickedBlock().getRelative(event.getBlockFace());

                if (block.getType().name().contains("LAVA")) {
                    if (!transformIntoWater(block)) {
                        player.sendMessage("§cVocê não pode usar o Thermo em áreas muito grandes.");
                    } else {
                        addCooldown(player.getUniqueId());
                        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 100, 0), true);
                    }
                } else if (block.getType().name().contains("WATER")) {
                    if (!transformIntoLava(block)) {
                        player.sendMessage("§cVocê não pode usar o Thermo em áreas muito grandes.");
                    } else {
                        addCooldown(player.getUniqueId());
                        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 0), true);
                    }
                } else {
                    player.sendMessage("§cVocê não pode usar o Thermo neste bloco.");
                }

            } else {
                player.setItemInHand(null);
            }
        }
    }


    private boolean transformIntoWater(final Block block) {
        final Set<Block> nearby = this.getNearbyBlocks(block, this::isLava);
        if (nearby.size() > 500) {
            return false;
        }
        final HashMap<Block, ThermoMaterialData> transform = new HashMap<>();
        for (final Block b2 : nearby) {
            if (b2.getType() == Material.LAVA) {
                transform.put(b2, new ThermoMaterialData(Material.WATER, b2.getData()));
            } else {
                transform.put(b2, new ThermoMaterialData(Material.STATIONARY_WATER, b2.getData()));
            }
            b2.setTypeId(0, false);
        }
        transform.forEach((b, m) -> b.setTypeIdAndData(m.getMaterial().getId(), m.getData(), false));
        return true;
    }

    private boolean transformIntoLava(final Block block) {
        final Set<Block> nearby = this.getNearbyBlocks(block, this::isWater);
        if (nearby.size() > 500) {
            return false;
        }
        final HashMap<Block, ThermoMaterialData> transform = new HashMap<>();
        for (final Block b2 : nearby) {
            if (b2.getType() == Material.WATER) {
                transform.put(b2, new ThermoMaterialData(Material.LAVA, b2.getData()));
            } else {
                transform.put(b2, new ThermoMaterialData(Material.STATIONARY_LAVA, b2.getData()));
            }
            b2.setTypeId(0, false);
        }
        transform.forEach((b, m) -> b.setTypeIdAndData(m.getMaterial().getId(), m.getData(), false));
        return true;
    }

    private boolean isWater(final Block block) {
        final Material material = block.getType();
        return material == Material.WATER || material == Material.STATIONARY_WATER;
    }

    private boolean isLava(final Block block) {
        final Material material = block.getType();
        return material == Material.LAVA || material == Material.STATIONARY_LAVA;
    }

    public Set<Block> getNearbyBlocks(final Block block, final Predicate<Block> predicate) {
        final Set<Block> blocks = new HashSet<>();
        blocks.add(block);
        this.getNearbyBlocks(block, predicate, blocks);
        return blocks;
    }

    public void getNearbyBlocks(final Block block, final Predicate<Block> predicate, final Set<Block> org) {
        if (org.size() > 500) {
            return;
        }
        for (final BlockFace bf : this.faces) {
            final Block next = block.getRelative(bf);
            if (next.getChunk().isLoaded() && !org.contains(next) && predicate.test(next)) {
                org.add(next);
                this.getNearbyBlocks(next, predicate, org);
            }
        }
    }

    private static class ThermoMaterialData {

        private Material material;
        private byte data;

        public Material getMaterial() {
            return this.material;
        }

        public byte getData() {
            return this.data;
        }

        public void setMaterial(final Material material) {
            this.material = material;
        }

        public void setData(final byte data) {
            this.data = data;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof ThermoMaterialData)) {
                return false;
            }
            final ThermoMaterialData other = (ThermoMaterialData) o;
            if (!other.canEqual(this)) {
                return false;
            }
            if (this.getData() != other.getData()) {
                return false;
            }
            final Object this$material = this.getMaterial();
            final Object other$material = other.getMaterial();
            if (this$material == null) {
                return other$material == null;
            } else return this$material.equals(other$material);
        }

        protected boolean canEqual(final Object other) {
            return other instanceof ThermoMaterialData;
        }

        @Override
        public int hashCode() {
            int result = 1;
            result = result * 59 + this.getData();
            final Object $material = this.getMaterial();
            result = result * 59 + (($material == null) ? 43 : $material.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "ThermoKit.ThermoMaterialData(material=" + this.getMaterial() + ", data=" + this.getData() + ")";
        }

        public ThermoMaterialData(final Material material, final byte data) {
            this.material = material;
            this.data = data;
        }
    }

    private final BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST};

}
