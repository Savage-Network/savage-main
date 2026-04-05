package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Expel extends Kit {

    public Expel(HungerGames hungerGames) {
        super(hungerGames);
        setKitCategory(KitCategory.STRATEGY);
        setIcon(Pattern.of(Material.HOPPER));
        setItems(new ItemFactory(Material.HOPPER).setName("§aExpelir").setDescription("§7Kit Expel").getStack());
        setPrice(35000);
        setCooldown(20);
        setReleasedAt(1642906800000L);
    }

    @Variable(name = "hg.kit.expel.push_distance", permission = Rank.ADMINISTRATOR)
    public double push_distance = 8;

    @Variable(name = "hg.kit.expel.vector_multiply", permission = Rank.ADMINISTRATOR)
    public double vector_multiply = 2.8D;

    @Variable(name = "hg.kit.expel.vector_y", permission = Rank.ADMINISTRATOR)
    public double vector_y = 0.4D;

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (!isItem(player.getItemInHand()))
            return;

        event.setCancelled(true);

        if (!ACCEPTABLES_INTERACT.contains(event.getAction()))
            return;

        if (!isUser(player))
            return;

        if (checkInvincibility(player))
            return;

        if (isCooldown(player)) {
            dispatchCooldown(player);
            return;
        }

        player.updateInventory();

        addCooldown(player.getUniqueId());

        for (Entity entity : player.getNearbyEntities(push_distance, push_distance / 2, push_distance)) {
            if (entity instanceof Player) {
                Player target = (Player) entity;

                User user = User.fetch(target.getUniqueId());

                if (!user.isAlive())
                    continue;

                Location playerCenterLocation = player.getEyeLocation();
                Location playerToThrowLocation = target.getEyeLocation();

                double x = playerToThrowLocation.getX() - playerCenterLocation.getX();
                double y = playerToThrowLocation.getY() - playerCenterLocation.getY();
                double z = playerToThrowLocation.getZ() - playerCenterLocation.getZ();

                Vector throwVector = new Vector(x, y, z);

                throwVector.normalize();
                throwVector.multiply(vector_multiply);
                throwVector.setY(vector_y);

                target.setVelocity(throwVector);
                target.damage(5D);
            }
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 0), true);
    }

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

}
