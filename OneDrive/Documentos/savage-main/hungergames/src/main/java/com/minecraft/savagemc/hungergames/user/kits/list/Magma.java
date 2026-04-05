package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import net.minecraft.server.v1_8_R3.DamageSource;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Random;

public class Magma extends Kit {

    public Magma(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.MAGMA_CREAM));
        setPrice(35000);
        setKitCategory(KitCategory.COMBAT);
    }

    private final Random random = Constants.RANDOM;

    @Variable(name = "hg.kit.magma.apply_chance", permission = Rank.ADMINISTRATOR)
    public int chance = 33;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if ((event.getDamager() instanceof Player && event.getEntity() instanceof Player)) {
            Player attacker = (Player) event.getDamager();
            if (isUser(attacker)) {
                if (random.nextInt(100) <= chance)
                    event.getEntity().setFireTicks(90);
            }
        }
    }

    private final ImmutableSet<EntityDamageEvent.DamageCause> CANCEL_DAMAGES = Sets.immutableEnumSet(EntityDamageEvent.DamageCause.FIRE, EntityDamageEvent.DamageCause.FIRE_TICK, EntityDamageEvent.DamageCause.LAVA);

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (isPlayer(event.getEntity())) {
            if (isUser((Player) event.getEntity()) && CANCEL_DAMAGES.contains(event.getCause()))
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
            ((CraftPlayer) player).getHandle().damageEntity(DamageSource.DROWN, 3);
        }
    }

}
