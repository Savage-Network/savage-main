package com.minecraft.savagemc.hungergames.command;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TrollCommand implements BukkitInterface {

    @Command(name = "troll", rank = Rank.PRIMARY_MOD, platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context, Player target) {
        Player sender = context.getSender();

        if (target == null || !sender.canSee(target)) {
            context.info("target.not_found");
            return;
        }

        new Troll(sender, target).createMenu();
    }

    public final static class Troll {

        private final Player viewer;
        private final Player target;

        public Troll(final Player viewer, final Player target) {
            this.viewer = viewer;
            this.target = target;
        }

        public void createMenu() {
            Inventory inventory = Bukkit.createInventory(null, 9, "Troll: " + target.getName());

            inventory.setItem(0, new InteractableItem(new ItemFactory(Material.IRON_FENCE).setName("§8Cage").getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    Block center = target.getLocation().getBlock().getRelative(0, 10, 0);
                    while (center.getType().isSolid())
                        center = center.getRelative(0, 10, 0);
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            center.getRelative(x, -1, z).setType(Material.BEDROCK);
                            center.getRelative(x, 2, z).setType(Material.BEDROCK);
                            if (x != 0 || z != 0)
                                center.getRelative(x, 0, z).setType(Material.BEDROCK);
                        }
                    }
                    target.teleport(center.getLocation().clone().add(0.5D, 0.25D, 0.5D));
                    viewer.sendMessage("§eVocê prendeu §b" + target.getName() + ".");
                    return true;
                }
            }).getItemStack());

            inventory.setItem(1, new InteractableItem(new ItemFactory(Material.TNT).setName("§cExplode").getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    target.getWorld().createExplosion(target.getLocation(), 2.0F);
                    viewer.sendMessage("§eVocê explodiu §b" + target.getName() + ".");
                    return true;
                }
            }).getItemStack());

            inventory.setItem(2, new InteractableItem(new ItemFactory(Material.CHEST).setName("§eItem Shuffle").getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    PlayerInventory inv = target.getInventory();
                    List<ItemStack> list = Arrays.asList(inv.getContents());
                    Collections.shuffle(list, Constants.RANDOM);
                    ItemStack[] array = list.toArray(new ItemStack[0]);
                    inv.setContents(array);

                    viewer.sendMessage("§eVocê bagunçou o inventário de §b" + target.getName() + ".");
                    return true;
                }
            }).getItemStack());

            inventory.setItem(3, new InteractableItem(new ItemFactory(Material.WEB).setName("§7Spider Cage").getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    Block center = target.getLocation().getBlock();
                    for (int x = -1; x <= 1; x++) {
                        for (int y = -1; y <= 1; y++) {
                            for (int z = -1; z <= 1; z++) {
                                Block rel = center.getRelative(x, y, z);
                                if (!rel.getType().isSolid())
                                    rel.setType(Material.WEB, false);
                            }
                        }
                    }
                    target.teleport(center.getLocation().clone().add(0.5D, 0.25D, 0.5D));
                    viewer.sendMessage("§eVocê prendeu §b" + target.getName() + " §eem uma teia.");
                    return true;
                }
            }).getItemStack());

            inventory.setItem(4, new InteractableItem(new ItemFactory(Material.LAVA_BUCKET).setName("§cLava Well").getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    final World world = target.getWorld();
                    final Location location = target.getLocation();

                    Location tl = new Location(world, location.getX(), location.getY() - 1.0D, location.getZ());
                    Location tl1 = new Location(world, location.getX(), location.getY() - 1.0D, location.getZ() + 1.0D);
                    Location tl2 = new Location(world, location.getX(), location.getY() - 1.0D, location.getZ() - 1.0D);
                    Location tl3 = new Location(world, location.getX() + 1.0D, location.getY() - 1.0D, location.getZ() + 1.0D);
                    Location tl4 = new Location(world, location.getX() - 1.0D, location.getY() - 1.0D, location.getZ() - 1.0D);
                    Location tl5 = new Location(world, location.getX() - 1.0D, location.getY() - 1.0D, location.getZ() + 1.0D);
                    Location tl6 = new Location(world, location.getX() + 1.0D, location.getY() - 1.0D, location.getZ() - 1.0D);
                    Location tl7 = new Location(world, location.getX() + 1.0D, location.getY() - 1.0D, location.getZ());
                    Location tl8 = new Location(world, location.getX() - 1.0D, location.getY() - 1.0D, location.getZ());
                    Location tl19 = new Location(world, location.getX(), location.getY() - 2.0D, location.getZ());
                    Location tl20 = new Location(world, location.getX(), location.getY() - 2.0D, location.getZ() + 1.0D);
                    Location tl21 = new Location(world, location.getX(), location.getY() - 2.0D, location.getZ() - 1.0D);
                    Location tl22 = new Location(world, location.getX() + 1.0D, location.getY() - 2.0D, location.getZ() + 1.0D);
                    Location tl23 = new Location(world, location.getX() - 1.0D, location.getY() - 2.0D, location.getZ() - 1.0D);
                    Location tl24 = new Location(world, location.getX() - 1.0D, location.getY() - 2.0D, location.getZ() + 1.0D);
                    Location tl25 = new Location(world, location.getX() + 1.0D, location.getY() - 2.0D, location.getZ() - 1.0D);
                    Location tl26 = new Location(world, location.getX() + 1.0D, location.getY() - 2.0D, location.getZ());
                    Location tl27 = new Location(world, location.getX() - 1.0D, location.getY() - 2.0D, location.getZ());
                    Location tl28 = new Location(world, location.getX() + 2.0D, location.getY(), location.getZ() + 2.0D);
                    Location tl29 = new Location(world, location.getX() - 2.0D, location.getY(), location.getZ() - 2.0D);
                    Location tl30 = new Location(world, location.getX() + 2.0D, location.getY(), location.getZ() - 2.0D);
                    Location tl31 = new Location(world, location.getX() - 2.0D, location.getY(), location.getZ() + 2.0D);
                    Location tl32 = new Location(world, location.getX() + 2.0D, location.getY(), location.getZ());
                    Location tl33 = new Location(world, location.getX() - 2.0D, location.getY(), location.getZ());
                    Location tl34 = new Location(world, location.getX(), location.getY(), location.getZ() + 2.0D);
                    Location tl35 = new Location(world, location.getX(), location.getY(), location.getZ() - 2.0D);
                    Location tl36 = new Location(world, location.getX() + 2.0D, location.getY(), location.getZ() + 1.0D);
                    Location tl37 = new Location(world, location.getX() - 2.0D, location.getY(), location.getZ() - 1.0D);
                    Location tl38 = new Location(world, location.getX() + 1.0D, location.getY(), location.getZ() - 2.0D);
                    Location tl39 = new Location(world, location.getX() - 1.0D, location.getY(), location.getZ() + 2.0D);
                    Location tl40 = new Location(world, location.getX() + 1.0D, location.getY(), location.getZ() + 2.0D);
                    Location tl41 = new Location(world, location.getX() - 1.0D, location.getY(), location.getZ() - 2.0D);
                    Location tl42 = new Location(world, location.getX() - 2.0D, location.getY(), location.getZ() + 1.0D);
                    Location tl43 = new Location(world, location.getX() + 2.0D, location.getY(), location.getZ() - 1.0D);

                    world.getBlockAt(tl).setType(Material.LAVA);
                    world.getBlockAt(tl1).setType(Material.LAVA);
                    world.getBlockAt(tl2).setType(Material.LAVA);
                    world.getBlockAt(tl3).setType(Material.LAVA);
                    world.getBlockAt(tl4).setType(Material.LAVA);
                    world.getBlockAt(tl5).setType(Material.LAVA);
                    world.getBlockAt(tl6).setType(Material.LAVA);
                    world.getBlockAt(tl7).setType(Material.LAVA);
                    world.getBlockAt(tl8).setType(Material.LAVA);
                    world.getBlockAt(tl19).setType(Material.LAVA);
                    world.getBlockAt(tl20).setType(Material.LAVA);
                    world.getBlockAt(tl21).setType(Material.LAVA);
                    world.getBlockAt(tl22).setType(Material.LAVA);
                    world.getBlockAt(tl23).setType(Material.LAVA);
                    world.getBlockAt(tl24).setType(Material.LAVA);
                    world.getBlockAt(tl25).setType(Material.LAVA);
                    world.getBlockAt(tl26).setType(Material.LAVA);
                    world.getBlockAt(tl27).setType(Material.LAVA);
                    world.getBlockAt(tl28).setType(Material.FENCE);
                    world.getBlockAt(tl29).setType(Material.FENCE);
                    world.getBlockAt(tl30).setType(Material.FENCE);
                    world.getBlockAt(tl31).setType(Material.FENCE);
                    world.getBlockAt(tl32).setType(Material.FENCE);
                    world.getBlockAt(tl33).setType(Material.FENCE);
                    world.getBlockAt(tl34).setType(Material.FENCE);
                    world.getBlockAt(tl35).setType(Material.FENCE);
                    world.getBlockAt(tl36).setType(Material.FENCE);
                    world.getBlockAt(tl37).setType(Material.FENCE);
                    world.getBlockAt(tl38).setType(Material.FENCE);
                    world.getBlockAt(tl39).setType(Material.FENCE);
                    world.getBlockAt(tl40).setType(Material.FENCE);
                    world.getBlockAt(tl41).setType(Material.FENCE);
                    world.getBlockAt(tl42).setType(Material.FENCE);
                    world.getBlockAt(tl43).setType(Material.FENCE);

                    viewer.sendMessage("§eVocê prendeu §b" + target.getName() + " §eem um poço de lava.");
                    return true;
                }
            }).getItemStack());

            viewer.openInventory(inventory);
        }

    }

    @Completer(name = "troll")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return getOnlineNicknames(context);
        return Collections.emptyList();
    }

}
