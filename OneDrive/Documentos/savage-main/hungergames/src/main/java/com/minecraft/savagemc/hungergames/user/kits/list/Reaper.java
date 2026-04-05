package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.Constants;
import com.minecraft.core.account.fields.Property;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.CooldownType;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.savagemc.hungergames.util.metadata.GameMetadata;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class Reaper extends Kit {

    public Reaper(HungerGames hungerGames) {
        super(hungerGames);
        setCooldown(20);
        setPrice(25000);
        setKitCategory(KitCategory.COMBAT);
        setIcon(Pattern.of(Material.SKULL_ITEM, 1));
        setItems(new ItemFactory(Material.WOOD_HOE).setName("§aArremessar").setDescription("§7Kit Reaper").getStack());
    }

    @Variable(name = "hg.kit.blink.max_uses", permission = Rank.ADMINISTRATOR)
    @Setter
    private int maxUses = 4;

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!ACCEPTABLES_INTERACT.contains(event.getAction()))
            return;

        if (!isUser(player))
            return;

        if (!isItem(player.getItemInHand()))
            return;

        event.setCancelled(true);

        if (isCooldown(player)) {
            dispatchCooldown(player);
            return;
        }

        User user = getUser(player.getUniqueId());

        Property property = user.getAccount().getProperty("hg.kit.reaper.uses", 0);

        int uses = property.getAsInt();
        uses++;
        property.setValue(uses);

        WitherSkull skull = player.launchProjectile(WitherSkull.class);
        skull.setYield(skull.getYield() * 0.25F);
        skull.setVelocity(skull.getVelocity().clone().multiply(1.1)); /*Constants.RANDOM.nextDouble() + 0.2*/
        skull.setMetadata("hg.kit.reaper", new GameMetadata((byte) 0));


        if (uses == maxUses) {
            addCooldown(player.getUniqueId());
            user.getAccount().removeProperty("hg.kit.reaper.uses");
        } else {
            addCooldown(player.getUniqueId(), CooldownType.DEFAULT, 1);
        }
    }


    private final Random random = Constants.RANDOM;

    @Variable(name = "hg.kit.reaper.apply_chance", permission = Rank.ADMINISTRATOR)
    private int chance = 15;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        if ((event.getDamager() instanceof Player && event.getEntity() instanceof Player)) {

            Player attacker = (Player) event.getDamager();
            if (isUser(attacker)) {
                if (random.nextInt(100) <= chance)
                    ((Player) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 2), true);
            }
        }
    }
}
