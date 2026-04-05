package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.account.fields.Property;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class Vacuum extends Kit {

    public Vacuum(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.EYE_OF_ENDER));
        setItems(new ItemFactory(Material.EYE_OF_ENDER).setName("§aPull").setDescription("§7Kit Vacuum").getStack());
        setCooldown(25);
        setKitCategory(KitCategory.STRATEGY);
        setPrice(25000);
    }

    @Variable(name = "hg.kit.vacuum.pull_distance", permission = Rank.ADMINISTRATOR)
    public double pull_distance = 12;

    @Variable(name = "hg.kit.vacuum.max_uses", permission = Rank.ADMINISTRATOR)
    public int maxUses = 3;

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

        User user = getUser(player.getUniqueId());

        Property property = user.getAccount().getProperty("hg.kit.vacuum.uses", 0);

        int uses = property.getAsInt();

        uses++;

        property.setValue(uses);

        player.updateInventory();

        for (Entity entity : player.getNearbyEntities(pull_distance, pull_distance / 2, pull_distance)) {
            if (entity instanceof Player) {
                Player target = (Player) entity;

                User user1 = User.fetch(target.getUniqueId());

                if (!user1.isAlive())
                    continue;

                Vector vector = player.getLocation().toVector().subtract(entity.getLocation().toVector());
                vector.multiply(0.3D);
                entity.setVelocity(vector);
            }
        }

        if (uses == maxUses) {
            addCooldown(player.getUniqueId());
            user.getAccount().removeProperty("hg.kit.vacuum.uses");
        }
    }

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

}
