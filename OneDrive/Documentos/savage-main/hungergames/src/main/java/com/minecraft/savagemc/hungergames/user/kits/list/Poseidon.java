package com.minecraft.savagemc.hungergames.user.kits.list;

import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Poseidon extends Kit {

    public Poseidon(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.WATER_BUCKET));
        setPrice(35000);
        setKitCategory(KitCategory.COMBAT);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (isPlayer(event.getEntity())) {
            if (isUser((Player) event.getEntity()) && event.getCause() == EntityDamageEvent.DamageCause.DROWNING)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onServerHeartbeat(ServerHeartbeatEvent event) {
        if (!event.isPeriodic(20))
            return;
        getPlugin().getUserStorage().getUsers().forEach(this::apply);
    }

    public void apply(User user) {
        if (!user.isAlive() || !isUser(user))
            return;

        Player player = user.getPlayer();

        if (player.getLocation().getBlock().getType().name().contains("WATER")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 10, 0), true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 0), true);
        }
    }

}
