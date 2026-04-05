package com.minecraft.savagemc.hungergames.util.selector.object;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.StringTimeUtils;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.DailyKit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DiaryKit implements BukkitInterface, Assistance, Listener {

    public void open(final User user) {
        final Player player = user.getPlayer();

        final DailyKit dailyKit = user.getDailyKit();

        final Inventory inventory = Bukkit.createInventory(player, 36, "Kit diário");

        inventory.setItem(11, new ItemFactory(Material.STAINED_GLASS_PANE).setDurability(5).setName("§aKit de movimentação").addItemFlag(ItemFlag.values()).getStack());
        inventory.setItem(13, new ItemFactory(Material.STAINED_GLASS_PANE).setDurability(14).setName("§aKit de combate").addItemFlag(ItemFlag.values()).getStack());
        inventory.setItem(15, new ItemFactory(Material.STAINED_GLASS_PANE).setDurability(11).setName("§aKit de estratégia").addItemFlag(ItemFlag.values()).getStack());

        Kit movement = HungerGames.getInstance().getKitStorage().getKit(dailyKit.getMovementKit());
        Kit strategy = HungerGames.getInstance().getKitStorage().getKit(dailyKit.getStrategyKit());
        Kit combat = HungerGames.getInstance().getKitStorage().getKit(dailyKit.getCombatKit());

        final Language language = user.getAccount().getLanguage();

        inventory.setItem(20, new ItemFactory(movement.getIcon().getMaterial()).setDurability(movement.getIcon().getData()).setName("§a" + movement.getName()).setDescription("§7" + movement.getDescrition(language) + "\n\n" + "§eClique para selecionar!").getStack());
        inventory.setItem(22, new ItemFactory(combat.getIcon().getMaterial()).setDurability(combat.getIcon().getData()).setName("§a" + combat.getName()).setDescription("§7" + combat.getDescrition(language) + "\n\n" + "§eClique para selecionar!").getStack());
        inventory.setItem(24, new ItemFactory(strategy.getIcon().getMaterial()).setDurability(strategy.getIcon().getData()).setName("§a" + strategy.getName()).setDescription("§7" + strategy.getDescrition(language) + "\n\n" + "§eClique para selecionar!").getStack());

        final int rollPrice = this.getRollPrice(dailyKit.getAgainRolls());

        inventory.setItem(35, rollPrice != 50000 ? new ItemFactory(Material.GOLD_NUGGET).setName("§aRolar novamente").setDescription("§7Nenhum kit te interessou? Role novamente!\n\n§7Custo: §6" + rollPrice + " coins").getStack() : new ItemFactory(Material.INK_SACK).setName("§cRolar novamente").setDescription("§7Você só pode rolar 3 vezes.").setDurability(7).getStack());

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent event) {
        if (!event.hasItem()) return;

        final ItemStack itemStack = event.getItem();
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemStack.getType() == Material.STORAGE_MINECART && itemMeta != null && itemMeta.hasDisplayName()) {
            final Player player = event.getPlayer();

            final User user = User.fetch(player.getUniqueId());
            final DailyKit dailyKit = user.getDailyKit();

            if (System.currentTimeMillis() > user.getDailyKit().getExpiration()) {

                dailyKit.setExpiration(convert("1d"));
                dailyKit.setAgainRolls(0);

                Kit movement = HungerGames.getInstance().getKitStorage().getRandomKit(KitCategory.MOVEMENT);
                Kit strategy = HungerGames.getInstance().getKitStorage().getRandomKit(KitCategory.STRATEGY);
                Kit combat = HungerGames.getInstance().getKitStorage().getRandomKit(KitCategory.COMBAT);

                dailyKit.setMovementKit(movement.getName());
                dailyKit.setStrategyKit(strategy.getName());
                dailyKit.setCombatKit(combat.getName());

                dailyKit.setChosenKit("...");

                open(user);

                user.getAccount().getData(Columns.HG_DAILY_KITS).setData(dailyKit.toJson());

                Bukkit.getScheduler().runTaskAsynchronously(HungerGames.getInstance(), () -> user.getAccount().getDataStorage().saveTable(Tables.HUNGERGAMES));
            } else {
                if (dailyKit.getChosenKit().equalsIgnoreCase("..."))
                    open(user);
                else {
                    player.sendMessage("§aO seu kit diário é o " + dailyKit.getChosenKit() + ".");
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, 3F, 3F);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClickInventory(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;

        final ItemStack item = event.getCurrentItem();

        if (item == null)
            return;

        if (item.getType() == Material.AIR)
            return;

        if (event.getClickedInventory() == null || !event.getClickedInventory().getName().equals("Kit diário"))
            return;

        event.setCancelled(true);

        final Player player = (Player) event.getWhoClicked();

        final User user = User.fetch(player.getUniqueId());
        final DailyKit dailyKit = user.getDailyKit();

        final Account account = user.getAccount();

        if (item.getType() == Material.GOLD_NUGGET) {
            final int price = getRollPrice(dailyKit.getAgainRolls());

            if (account.getData(Columns.HG_COINS).getAsInt() < price) {
                player.sendMessage("§cVocê não possui coins o suficiente para sortear novamente.");
                player.playSound(player.getLocation(), Sound.NOTE_BASS_DRUM, 3F, 3F);
                return;
            }

            account.removeInt(price, Columns.HG_COINS);
            dailyKit.incrementRolls();

            Kit movement = HungerGames.getInstance().getKitStorage().getRandomKit(KitCategory.MOVEMENT);
            Kit strategy = HungerGames.getInstance().getKitStorage().getRandomKit(KitCategory.STRATEGY);
            Kit combat = HungerGames.getInstance().getKitStorage().getRandomKit(KitCategory.COMBAT);

            dailyKit.setCombatKit(combat.getName());
            dailyKit.setMovementKit(movement.getName());
            dailyKit.setStrategyKit(strategy.getName());

            dailyKit.setChosenKit("...");

            final Language language = account.getLanguage();

            Inventory inventory = event.getClickedInventory();

            inventory.setItem(20, new ItemFactory(movement.getIcon().getMaterial()).setDurability(movement.getIcon().getData()).setName("§a" + movement.getName()).setDescription("§7" + movement.getDescrition(language) + "\n\n" + "§eClique para selecionar!").getStack());
            inventory.setItem(22, new ItemFactory(combat.getIcon().getMaterial()).setDurability(combat.getIcon().getData()).setName("§a" + combat.getName()).setDescription("§7" + combat.getDescrition(language) + "\n\n" + "§eClique para selecionar!").getStack());
            inventory.setItem(24, new ItemFactory(strategy.getIcon().getMaterial()).setDurability(strategy.getIcon().getData()).setName("§a" + strategy.getName()).setDescription("§7" + strategy.getDescrition(language) + "\n\n" + "§eClique para selecionar!").getStack());

            player.playSound(player.getLocation(), Sound.NOTE_PLING, 3.3F, 3.3F);
            onInteractEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, new ItemFactory(Material.STORAGE_MINECART).setName("daily_kit").getStack(), null, null));

            account.getData(Columns.HG_DAILY_KITS).setData(dailyKit.toJson());

            Bukkit.getScheduler().runTaskAsynchronously(HungerGames.getInstance(), () -> user.getAccount().getDataStorage().saveTable(Tables.HUNGERGAMES));
            return;
        }

        if (item.getType() == Material.STAINED_GLASS_PANE || item.getType() == Material.INK_SACK)
            return;

        final Kit kit = HungerGames.getInstance().getKitStorage().getKit(item.getItemMeta().getDisplayName().replace("§a", ""));

        user.giveKit(kit, dailyKit.getExpiration());

        dailyKit.setChosenKit(kit.getName());

        player.sendMessage("§eVocê escolheu o kit §b" + kit.getName() + "§e como seu kit diário.");

        player.playSound(player.getLocation(), Sound.NOTE_PLING, 3F, 3F);
        player.closeInventory();

        user.getAccount().getData(Columns.HG_DAILY_KITS).setData(dailyKit.toJson());

        Bukkit.getScheduler().runTaskAsynchronously(HungerGames.getInstance(), () -> account.getDataStorage().saveTable(Tables.HUNGERGAMES));
    }

    protected int getRollPrice(int againRolls) {
        if (againRolls == 0) return 500;
        if (againRolls == 1) return 750;
        if (againRolls == 2) return 1000;
        return 50000;
    }

    public long convert(String time) {
        try {
            return StringTimeUtils.parseDateDiff(time, true);
        } catch (Exception ex) {
            return -1L;
        }
    }

}
