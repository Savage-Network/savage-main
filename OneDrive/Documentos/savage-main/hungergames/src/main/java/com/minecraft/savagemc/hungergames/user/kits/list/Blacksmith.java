package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Blacksmith extends Kit {

    public Blacksmith(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.ANVIL));
        setKitCategory(KitCategory.STRATEGY);
        setItems(new ItemFactory(Material.ANVIL).setName("§aReparar itens").setDescription("§7Kit Blacksmith").getStack());
        setCooldown(150);
    }

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        final Player player = event.getPlayer();

        if (!ACCEPTABLES_INTERACT.contains(event.getAction()))
            return;

        if (!isUser(player))
            return;

        if (!isItem(player.getItemInHand()))
            return;

        if (isCooldown(player) || isCombat(player)) {
            dispatchCooldown(player);
            return;
        }

        event.setCancelled(true);

        player.sendMessage("§aVocê reparou todos os itens do seu inventário.");
        player.playSound(player.getLocation(), Sound.ANVIL_USE, 1F, 1F);

        addCooldown(player.getUniqueId());

        for (ItemStack contents : player.getInventory().getContents()) {
            if (contents == null || contents.getType() == Material.INK_SACK)
                continue;
            contents.setDurability((byte) 0);
        }

        for (ItemStack armour : player.getInventory().getArmorContents()) {
            if (armour == null)
                continue;
            armour.setDurability((byte) 0);
        }

        player.updateInventory();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(BlockPlaceEvent event) {

        Player player = event.getPlayer();

        ItemStack itemStack = player.getItemInHand();

        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;

        if (isUser(player) && isItem(itemStack)) {

            event.setCancelled(true);
            player.updateInventory();

            if (checkInvincibility(player))
                return;

            if (isCombat(player) || isCooldown(player)) {
                dispatchCooldown(player);
                return;
            }

            player.sendMessage("§aVocê reparou todos os itens do seu inventário.");
            player.playSound(player.getLocation(), Sound.ANVIL_USE, 1F, 1F);

            addCooldown(player.getUniqueId());

            for (ItemStack contents : player.getInventory().getContents()) {
                if (contents == null || contents.getType() == Material.INK_SACK)
                    continue;
                contents.setDurability((byte) 0);
            }

            for (ItemStack armour : player.getInventory().getArmorContents()) {
                if (armour == null)
                    continue;
                armour.setDurability((byte) 0);
            }

            player.updateInventory();
        }
    }

}
