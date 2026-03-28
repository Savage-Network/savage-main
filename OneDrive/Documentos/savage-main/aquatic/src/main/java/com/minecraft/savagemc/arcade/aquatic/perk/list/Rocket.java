package com.minecraft.savagemc.arcade.aquatic.perk.list;

import com.minecraft.savagemc.arcade.aquatic.perk.Perk;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Rocket extends Perk implements Listener {

    private static final Set<Player> doubleJumpEnabled = new HashSet<>();

    public Rocket() {
        super("Rocket", Item.of(Material.LEATHER_BOOTS), RankType.MEMBER, 0, 8);

        setLore("§7Um salto de coragem, um passo para a vitória.");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (isUsing(player.getUniqueId()) && player.isOnGround()) {
            if (hasCooldown(player, false)) return;

            player.setAllowFlight(true);
            doubleJumpEnabled.add(player);
        }
    }

    @EventHandler
    public void onDoubleJump(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (isUsing(player.getUniqueId())) {
            if (hasCooldown(player)) return;

            if (!doubleJumpEnabled.contains(player)) return;

            event.setCancelled(true);

            player.setAllowFlight(false);
            doubleJumpEnabled.remove(player);

            Vector jump = player.getLocation().getDirection();

            jump.multiply(0.5);
            jump.setY(0.8);

            player.setVelocity(jump);

            player.playSound(Sound.MAGMACUBE_JUMP);

            applyCooldown(player);
        }
    }
}
