package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Tag;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Sabotager extends Kit {

    protected final ImmutableSet<Action> actionImmutableSet = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);
    public static final List<Integer> allowedSlots = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

    public Sabotager(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.TRIPWIRE_HOOK));
        setKitCategory(KitCategory.STRATEGY);
        setItems(new ItemFactory(Material.TRIPWIRE_HOOK).setName("§aSabotage").setDescription("§7Kit Sabotager").getStack());
        setPrice(50000);
        setActive(false, false);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!actionImmutableSet.contains(event.getAction()))
            return;

        if (!isUser(player))
            return;

        if (!isItem(player.getItemInHand()))
            return;

        if (checkInvincibility(player))
            return;

        if (isCooldown(player)) {
            dispatchCooldown(player);
            return;
        }

        new SabotageList(player).open();

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null)
            return;
        if (event.getClickedInventory() == null)
            return;
        if (!event.getClickedInventory().getName().equals("Sabotage"))
            return;
        if (event.getCurrentItem().getType() != Material.SKULL_ITEM)
            return;

        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());

        if (nmsStack != null && nmsStack.hasTag() && nmsStack.getTag().hasKey("sabotagelist")) {

            final String nbtTagString = nmsStack.getTag().getString("sabotagelist");

            final Player player = Bukkit.getPlayer(UUID.fromString(nbtTagString));

            if (player == null)
                return;

            final Player sabotager = (Player) event.getWhoClicked();

            new Sabotage(sabotager, player).open();
        }
    }

    public static final class Sabotage implements Assistance {

        private final Player sabotager, sabotaged;

        public Sabotage(final Player sabotager, final Player sabotaged) {
            this.sabotager = sabotager;
            this.sabotaged = sabotaged;
        }

        public void open() {
            final int kills = User.fetch(sabotager.getUniqueId()).getKills();

            final List<ItemStack> itemStacks = new ArrayList<>();

            for (final Sabotages sabotages : Sabotages.values()) {
                itemStacks.add(new InteractableItem(new ItemFactory(sabotages.getPattern().getMaterial()).setName("§a" + sabotages.getName()).setDescription("§7" + sabotages.getDescription() + "\n\n" + "§7Requer: §f" + sabotages.getKills() + " kills." + "\n\n" + (kills >= sabotages.getKills() ? "§eClique para sabotar." : "§cVocê não tem kills o suficiente.")).getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        sabotages.getExecutor().execute(sabotager, sabotaged);
                        return true;
                    }
                }).getItemStack());
            }

            Selector.builder().withName("Sabotage modes").withAllowedSlots(Sabotager.allowedSlots).withSize(27).withNextPageSlot(44).withPreviousPageSlot(36).withItems(itemStacks).build().open(sabotager);
        }

    }

    @AllArgsConstructor
    @Getter
    public enum Sabotages {

        BLINDNESS(Pattern.of(Material.EYE_OF_ENDER), "Blindness", "Cegue seu oponente por 10 segundos.", 1, (sabotager, sabotaged) -> {
            sabotaged.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0), true);
            sabotaged.sendMessage("§aVocê foi sabotado por " + sabotager.getName() + "!");
        }),

        ITEM_SHUFFLE(Pattern.of(Material.CHEST), "Item Shuffle", "Embaralhe o inventário de seu oponente.", 1, (sabotager, sabotaged) -> {
            PlayerInventory inv = sabotaged.getInventory();
            List<ItemStack> list = Arrays.asList(inv.getContents());
            Collections.shuffle(list, Constants.RANDOM);
            inv.setContents(list.toArray(new ItemStack[0]));
            sabotaged.sendMessage("§aVocê foi sabotado por " + sabotager.getName() + "!");
        }),

        SPIDER_CAGE(Pattern.of(Material.WEB), "Spider Web", "Prenda seu oponente em uma grande teia de aranha.", 3, (sabotager, sabotaged) -> {
            Block center = sabotaged.getLocation().getBlock();
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        Block rel = center.getRelative(x, y, z);
                        if (!rel.getType().isSolid())
                            rel.setType(Material.WEB, false);
                    }
                }
            }
            sabotaged.teleport(center.getLocation().clone().add(0.5D, 0.25D, 0.5D));
            sabotaged.sendMessage("§aVocê foi sabotado por " + sabotager.getName() + "!");
        }),

        DROP_HOTBAR(Pattern.of(Material.HOPPER), "Blindness", "Drope todos os itens de sua hotbar.", 3, (sabotager, sabotaged) -> {

            final PlayerInventory inventory = sabotaged.getInventory();
            final World world = sabotaged.getWorld();
            final Location location = sabotaged.getLocation();

            for (int i = 0; i < 9; i++) {
                final ItemStack itemStack = inventory.getItem(i);

                if (itemStack == null)
                    continue;

                world.dropItem(location, itemStack);

                inventory.setItem(0, new ItemStack(Material.AIR));
            }

            sabotaged.sendMessage("§aVocê foi sabotado por " + sabotager.getName() + "!");
        }),

        LAVA_WEEL(Pattern.of(Material.LAVA_BUCKET), "Lava Well", "Prenda seu inimigo em um poço de lava.", 5, (sabotager, sabotaged) -> {
            final World world = sabotaged.getWorld();
            final Location location = sabotaged.getLocation();

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

            sabotaged.sendMessage("§aVocê foi sabotado por " + sabotager.getName() + "!");
        });

        private final Pattern pattern;
        private final String name, description;
        private int kills;
        private final Executor executor;

        public interface Executor {
            void execute(Player sabotager, Player sabotaged);
        }

    }

    public static final class SabotageList implements Assistance {

        private final Player player;

        public SabotageList(final Player player) {
            this.player = player;
        }

        public void open() {
            final List<ItemStack> itemStacks = new ArrayList<>();

            for (User user : getPlugin().getUserStorage().getAliveUsers()) {
                final Player p = user.getPlayer();
                if (player.getLocation().distanceSquared(p.getLocation()) > 2500)
                    continue;
                if (((Gladiator) getKit("Gladiator")).isGladiator(p))
                    continue;

                itemStacks.add(addTag(head(user), user.getUniqueId()));
            }

            Selector.builder().withName("Sabotage").withAllowedSlots(Sabotager.allowedSlots).withSize(45).withNextPageSlot(44).withPreviousPageSlot(36).withItems(itemStacks).build().open(player);
        }

        private ItemStack head(User user) {
            final ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            final SkullMeta meta = (SkullMeta) skull.getItemMeta();

            Tag tag = user.getAccount().getProperty("account_tag").getAs(Tag.class);
            meta.setDisplayName(tag.getFormattedColor() + user.getName());

            List<String> lore = new ArrayList<>();

            lore.add(" ");
            lore.add("§eClique para sabotar.");

            meta.setLore(lore);
            skull.setItemMeta(meta);
            meta.setOwner(user.getName());
            skull.setItemMeta(meta);

            return skull;
        }

        public ItemStack addTag(ItemStack stack, UUID uuid) {
            net.minecraft.server.v1_8_R3.ItemStack nmsCopy = CraftItemStack.asNMSCopy(stack);
            NBTTagCompound nbtTagCompound = (nmsCopy.hasTag()) ? nmsCopy.getTag() : new NBTTagCompound();
            nbtTagCompound.set("sabotagelist", new NBTTagString(uuid.toString()));
            nmsCopy.setTag(nbtTagCompound);
            return CraftItemStack.asBukkitCopy(nmsCopy);
        }
    }

}
