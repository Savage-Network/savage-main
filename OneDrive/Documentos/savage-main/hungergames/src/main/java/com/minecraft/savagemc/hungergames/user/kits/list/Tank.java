/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class Tank extends Kit {

    public Tank(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.TNT));
        setKitCategory(KitCategory.STRATEGY);
        setPrice(20000);
    }

    private final ImmutableSet<EntityDamageEvent.DamageCause> FILTER_CAUSE = Sets.immutableEnumSet(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION);

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (FILTER_CAUSE.contains(event.getCause()) && isPlayer(event.getEntity())) {
            if (isUser((Player) event.getEntity()))
                event.setCancelled(true);
        }
    }

    @Override
    public void appreciate(LivingUserDieEvent event) {
        Location location = event.getUser().getPlayer().getLocation();
        location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), 3F, true, false);
    }
}
