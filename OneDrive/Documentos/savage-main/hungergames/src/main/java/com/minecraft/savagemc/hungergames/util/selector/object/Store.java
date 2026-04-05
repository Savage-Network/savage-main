package com.minecraft.savagemc.hungergames.util.selector.object;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.translation.Language;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Store implements BukkitInterface, Assistance {

    private User owner;
    private Selector selector;

    private final static List<Integer> allowedSlots = Chooser.allowedSlots;

    public Store(User user) {
        this.owner = user;
    }

    public Store build() {
        User user = this.owner;
        Account account = user.getAccount();

        Language language = account.getLanguage();
        int coins = account.getData(Columns.HG_COINS).getAsInt();

        List<Kit> kitList = new ArrayList<>(HungerGames.getInstance().getKitStorage().getKits());
        kitList.removeIf(kit -> !kit.isActive() || kit.isNone() || owner.hasKit(kit, 1) ||
                kit.getClass().getSimpleName().equals("Toxic") || kit.getClass().getSimpleName().equals("Expel"));

        if (kitList.isEmpty()) {
            this.selector = Selector.builder().withAllowedSlots(allowedSlots).withCustomItem(49, new ItemFactory(Material.EMERALD).setName("§7Coins: §6" + Constants.DECIMAL_FORMAT.format(coins)).getStack()).withCustomItem(22, new ItemFactory(Material.STAINED_GLASS_PANE).setDurability(14).setName("§cVocê já possui todos os kits! :)").getStack()).withName(language.translate("container.buy_kit.title")).withSize(54).build();
            return this;
        }

        List<ItemStack> itemStacks = new ArrayList<>();

        kitList.forEach(kit -> {
            int kitPrice = kit.getPrice();
            ItemStack itemStack = new ItemFactory(kit.getIcon().getMaterial()).setDescription(kit.getDescrition(user.getAccount().getLanguage()) + "\n\n" + "§7Preço: §6" + Constants.DECIMAL_FORMAT.format(kitPrice) + " coins" + "\n\n" + (coins >= kitPrice ? "§eClique para adquirir!" : "§cVocê não possui coins o suficiente.")).setName("§c" + kit.getDisplayName()).setDurability(kit.getIcon().getData()).addItemFlag(ItemFlag.values()).getStack();
            itemStacks.add(new InteractableItem(addTag(itemStack, kit.getName()), interact).getItemStack());
        });

        this.selector = Selector.builder().withAllowedSlots(allowedSlots).withCustomItem(49, new ItemFactory(Material.EMERALD).setName("§7Coins: §6" + Constants.DECIMAL_FORMAT.format(coins)).getStack()).withName(language.translate("container.buy_kit.title")).withSize(54).withItems(itemStacks).build();
        return this;
    }

    private static InteractableItem.Interact interact = new InteractableItem.Interact() {
        @Override
        public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
            if (nmsStack != null && nmsStack.hasTag() && nmsStack.getTag().hasKey("kit")) {

                User user = User.fetch(player.getUniqueId());
                Kit kit = HungerGames.getInstance().getKitStorage().getKit(nmsStack.getTag().getString("kit"));

                if (user.hasKit(kit, 1))
                    return true;

                Account account = user.getAccount();

                int coins = account.getData(Columns.HG_COINS).getAsInt();
                int kitPrice = kit.getPrice();

                if (coins < kitPrice)
                    return true;

                player.sendMessage("§aVocê adquiriu o kit §f" + kit.getDisplayName() + " §apor §f" + Constants.DECIMAL_FORMAT.format(kitPrice) + " §acoins.");
                player.closeInventory();

                account.removeInt(kitPrice, Columns.HG_COINS);
                user.giveKit(kit, -1);

                Bukkit.getScheduler().runTaskAsynchronously(HungerGames.getInstance(), () -> account.getDataStorage().saveTable(Tables.HUNGERGAMES));
            }
            return true;
        }
    };

    public Store open() {
        getSelector().open(getOwner().getPlayer());
        return this;
    }

    public ItemStack addTag(ItemStack stack, String name) {
        net.minecraft.server.v1_8_R3.ItemStack nmsCopy = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound nbtTagCompound = (nmsCopy.hasTag()) ? nmsCopy.getTag() : new NBTTagCompound();
        nbtTagCompound.set("kit", new NBTTagString(name));
        nmsCopy.setTag(nbtTagCompound);
        return CraftItemStack.asBukkitCopy(nmsCopy);
    }


}
