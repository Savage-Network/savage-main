package com.minecraft.savagemc.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Beastmaster extends Kit {

    public Beastmaster(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.MONSTER_EGG, 95));
        setKitCategory(KitCategory.COMBAT);
        setPrice(20000);
        setCooldown(10);
        setItems(new ItemFactory(Material.MONSTER_EGG).setDurability(95).setName("§aSummon").getStack());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && event.getAction().name().contains("RIGHT") && isUser(event.getPlayer()) && isItem(event.getItem())) {
            event.setCancelled(true);

            final Player player = event.getPlayer();

            if (checkInvincibility(player))
                return;

            if (isCooldown(player)) {
                dispatchCooldown(player);
                return;
            }

            int count = 0;
            for (final Entity entitiesByClass : player.getWorld().getEntitiesByClasses(Wolf.class)) {
                final Wolf wolf = (Wolf) entitiesByClass;
                if (wolf.getOwner() == player && ++count >= max_wolves) {
                    player.sendMessage("§cVocê só pode colocar " + max_wolves + " lobos.");
                    return;
                }
            }

            addCooldown(player.getUniqueId());

            final Wolf wolf = (Wolf) player.getWorld().spawnEntity(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(), EntityType.WOLF);

            wolf.setOwner(player);
            wolf.setTamed(true);
            wolf.setCollarColor(DyeColor.CYAN);
            wolf.setAdult();
            wolf.setCustomName("Lobo de §b" + player.getName());
        }
    }

    @Variable(name = "hg.kit.beastmaster.max_wolves", permission = Rank.ADMINISTRATOR)
    public int max_wolves = 4;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Wolf) {
            final Wolf damager = (Wolf) event.getDamager();
            if (damager.isTamed() && damager.getOwner() != null) {
                final User owner = getUser(damager.getOwner().getUniqueId());
                if (owner != null) {
                    final User user = User.fetch(event.getEntity().getUniqueId());
                    user.getCombatTag().addTag(owner, 15);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Wolf && isUser(event.getPlayer()) && isItem(event.getPlayer().getItemInHand()))
            event.setCancelled(true);
    }

}
