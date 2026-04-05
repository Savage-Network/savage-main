/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Hulk extends Kit {

    public Hulk(HungerGames hungerGames) {
        super(hungerGames);
        setCooldown(16);
        setIcon(Pattern.of(Material.SADDLE));
        setKitCategory(KitCategory.STRATEGY);
        setPrice(25000);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (isPlayer(event.getRightClicked())) {

            Player player = event.getPlayer();
            Player clicked = (Player) event.getRightClicked();

            if (player.isInsideVehicle() || clicked.isInsideVehicle())
                return;

            User user = User.fetch(player.getUniqueId());

            if (isUser(user) && user.isAlive()) {

                if (checkInvincibility(player))
                    return;

                if (isCooldown(player)) {
                    dispatchCooldown(player);
                    return;
                }

                if (!getUser(clicked.getUniqueId()).isAlive())
                    return;

                player.setPassenger(clicked);
                clicked.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 3), false);

                addCooldown(player.getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player && event.getEntity() instanceof Player))
            return;
        Player damager = (Player) event.getDamager();
        if (!isUser(damager))
            return;
        if (damager.getPassenger() != null && (damager.getItemInHand() == null || damager.getItemInHand().getType() == Material.AIR || damager.getItemInHand().getType() == Material.MUSHROOM_SOUP)) {
            Vector v = ((Player) event.getDamager()).getEyeLocation().getDirection().setY(0.7D).multiply(1.2F);
            event.getDamager().eject();
            run(() -> event.getEntity().setVelocity(v), 3L);
        }
    }
}
