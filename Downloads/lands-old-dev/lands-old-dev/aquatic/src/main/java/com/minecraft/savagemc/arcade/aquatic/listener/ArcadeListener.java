package com.minecraft.savagemc.arcade.aquatic.listener;

import com.minecraft.savagemc.arcade.aquatic.arcade.Arcade;
import com.minecraft.savagemc.arcade.aquatic.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.aquatic.user.User;
import com.minecraft.savagemc.core.arcade.ArcadeHolder;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.block.BlockApi;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import com.minecraft.savagemc.core.bukkit.manager.Manager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

import static com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil.hasWaterInLocation;

public class ArcadeListener implements Listener {

    private final Manager manager = BukkitCore.getManager();

    @EventHandler
    public synchronized void onSyncUpdate(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (ArcadeHolder holder : manager.getArcade().getArcades()) {
                if (!(holder instanceof Arcade)) continue;

                Arcade arcade = (Arcade) holder;

                List<Arena> arenaList = arcade.getRooms().stream().map(room -> (Arena) room).collect(Collectors.toList());

                arcade.checkAndGenerateArenas();

                arenaList.forEach(Arena::handlePulse);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockChestItems(InventoryClickEvent event) {
        // Bloquear interação com armadura
        if (event.getClickedInventory() != null && event.getClickedInventory().getType().equals(InventoryType.PLAYER)
                && event.getSlot() >= 36 && event.getSlot() <= 39) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onWoolPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        Block placedBlock = event.getBlockPlaced();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer()) return;

        Arena arena = user.getArena();

        if (arena.isPhase(RoomPhase.PLAYING) && !user.isProtected() && placedBlock.getType().equals(Material.WOOL)) {

            if (hasWaterInLocation(placedBlock.getLocation(), 1)) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(false);

            ItemStack hand = event.getItemInHand();

            hand.setAmount(hand.getMaxStackSize());

            player.setItemInHand(hand);
            player.updateInventory();

            BlockApi.handleBreakEffect(placedBlock.getLocation(), 3);
        }
    }
}
