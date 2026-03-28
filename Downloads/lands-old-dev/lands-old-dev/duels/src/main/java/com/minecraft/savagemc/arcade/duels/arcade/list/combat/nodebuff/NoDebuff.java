package com.minecraft.savagemc.arcade.duels.arcade.list.combat.nodebuff;

import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.savagemc.core.bukkit.manager.list.CooldownManager;
import com.minecraft.savagemc.core.event.IgnoreEvent;
import com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil;
import com.minecraft.savagemc.core.util.list.serialization.Serialization;
import com.minecraft.savagemc.arcade.duels.arcade.Arcade;
import com.minecraft.savagemc.arcade.duels.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.duels.arcade.objects.style.SidebarStyle;
import com.minecraft.savagemc.arcade.duels.user.User;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;

@IgnoreEvent
public class NoDebuff extends Arcade {

    private final CooldownManager cooldownManager;

    private final String pearlName;
    private final int pearlDuration;

    public NoDebuff(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.DUELS_NO_DEBUFF);

        this.cooldownManager = BukkitCore.getManager().getCooldown();

        this.pearlName = "Pérola";
        this.pearlDuration = 16;

        setStyle(SidebarStyle.LATENCY);
    }

    @Override
    public void handleHotbar(Player player, Arena arena) {
        if (arena.isValid(player)) {
            User user = (User) User.of(player.getUniqueId());

            String base64 = user.getMember().getBase64(ArcadeCategory.DUELS_NO_DEBUFF);

            if (base64 != null)
                Serialization.sendInventoryToPlayerFromBase64(player, base64);

            BukkitUtil.sendArmor(player, BukkitUtil.ArmorType.DIAMOND);
        } else
            handleDefaultHotbar(player);
    }

    @EventHandler
    public void onLaunchEnderPearl(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof EnderPearl) {
            EnderPearl pearl = (EnderPearl) event.getEntity();

            Player player = (Player) pearl.getShooter();

            if (isValid(player))
                cooldownManager.addCooldown(player.getUniqueId(), pearlName, pearlDuration);
        }
    }

    @EventHandler
    public void onEnderPearl(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isValid(player) && event.hasItem()) {
            ItemStack item = event.getItem();

            if (item != null && item.isType(Material.ENDER_PEARL)) {

                Cooldown cooldown = cooldownManager.getCooldown(player, pearlName);

                if (cooldown == null) return;

                event.setCancelled(true);

                player.sendMessage("§cAguarde " + new DecimalFormat("#.#").format(cooldown.getRemaining()) + "s para usar a " + pearlName + " novamente.");
                player.updateInventory();
            }
        }
    }
}
