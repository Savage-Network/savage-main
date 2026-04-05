package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.TileEntityFurnace;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftFurnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.UUID;

public class Crafter extends Kit {

    protected final Location workbench;
    protected final ImmutableSet<Action> actionImmutableSet = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);
    protected final HashMap<UUID, EntityFurnace> entityFurnaceHashMap = new HashMap<>();

    public Crafter(HungerGames hungerGames) {
        super(hungerGames);
        setItems(new ItemFactory(Material.NETHER_STAR).setName("§aMesa de trabalho").setDescription("§7Kit Crafter").getStack());
        setIcon(Pattern.of(Material.WORKBENCH));
        setKitCategory(KitCategory.STRATEGY);

        this.workbench = new Location(getGame().getWorld(), 1, 1, 1);
        this.workbench.getBlock().setType(Material.WORKBENCH);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!actionImmutableSet.contains(event.getAction()))
            return;

        final Player player = event.getPlayer();

        if (!isUser(player))
            return;

        if (!isItem(player.getItemInHand()))
            return;

        event.setCancelled(true);

        player.openWorkbench(this.workbench, true);
    }

    public static class EntityFurnace extends TileEntityFurnace {

        public EntityFurnace(EntityHuman entity) {
            this.world = entity.world;
        }

        public boolean a(EntityHuman entityhuman) {
            return true;
        }

        public int p() {
            return 0;
        }

        public void update() {
        }

        public Block q() {
            return Blocks.FURNACE;
        }

        public InventoryHolder getOwner() {
            CraftFurnace craftFurnace = new CraftFurnace(this.world.getWorld().getBlockAt(0, 0, 0));
            try {
                Field field = CraftFurnace.class.getDeclaredField("furnace");
                field.setAccessible(true);
                Field mfield = Field.class.getDeclaredField("modifiers");
                mfield.setAccessible(true);
                mfield.set(field, field.getModifiers() & 0xFFFFFFEF);
                field.set(craftFurnace, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return craftFurnace;
        }
    }

}
