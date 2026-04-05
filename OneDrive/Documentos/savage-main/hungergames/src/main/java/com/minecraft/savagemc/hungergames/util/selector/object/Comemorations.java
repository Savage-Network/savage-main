package com.minecraft.savagemc.hungergames.util.selector.object;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.translation.Language;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.celebrations.Celebration;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public class Comemorations implements BukkitInterface, Assistance {

    private User owner;
    private Selector selector;

    private final static List<Integer> allowedSlots = Chooser.allowedSlots;

    public Comemorations(User user) {
        this.owner = user;
    }

    public Comemorations build() {
        User user = this.owner;
        Account account = user.getAccount();

        Language language = account.getLanguage();
        int coins = account.getData(Columns.HG_COINS).getAsInt();

        List<ItemStack> itemStacks = new ArrayList<>();

        List<Celebration> celebrationList = new ArrayList<>(HungerGames.getInstance().getCelebrationStorage().getCelebrations());
        celebrationList.sort(Comparator.comparingInt(celebration -> celebration.getRarity().getId()));

        celebrationList.forEach(celebration -> {
            boolean hasCelebration = user.hasCelebration(celebration);

            final ItemFactory itemFactory = new ItemFactory(celebration.getIcon().getMaterial());

            if (user.getCelebration() == celebration) {
                itemFactory.addEnchantment(Enchantment.SILK_TOUCH, 1).addItemFlag(ItemFlag.HIDE_ENCHANTS);
            }

            itemFactory.setName((hasCelebration ? "§a" : "§c") + celebration.getDisplayName());
            itemFactory.addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
            itemFactory.setDurability(celebration.getIcon().getData());

            itemFactory.setDescription(celebration.getDescription() + "\n\n" + "§7Raridade: §r" + celebration.getRarity().getDisplayName() + (celebration.isFree ? "" : "\n" + "§7Disponível para " + celebration.getRank().getDefaultTag().getColor() + celebration.getRank().getName()) + "\n\n" + (hasCelebration ? user.getCelebration() == celebration ? "§cComemoração equipada." : "§eClique para selecionar." : "§cVocê não possui essa celebração."));

            itemStacks.add(new InteractableItem(addTag(itemFactory.getStack(), celebration.getName()), interact).getItemStack());
        });

        this.selector = Selector.builder().withAllowedSlots(allowedSlots).withCustomItem(49, new ItemFactory(Material.EMERALD).setName("§7Coins: §6" + Constants.DECIMAL_FORMAT.format(coins)).getStack()).withName(language.translate("container.comemorations.title")).withSize(54).withItems(itemStacks).build();
        return this;
    }

    private static InteractableItem.Interact interact = new InteractableItem.Interact() {
        @Override
        public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
            if (nmsStack != null && nmsStack.hasTag() && nmsStack.getTag().hasKey("celebration")) {

                final User user = User.fetch(player.getUniqueId());
                final Celebration celebration = HungerGames.getInstance().getCelebrationStorage().getCelebration(nmsStack.getTag().getString("celebration"));

                if (user.getCelebration() == celebration)
                    return true;

                if (!user.hasCelebration(celebration))
                    return true;

                player.sendMessage("§aComemoração de vitória selecionada: " + celebration.getDisplayName() + ".");
                player.closeInventory();

                user.setCelebration(celebration);
            }
            return true;
        }
    };

    public Comemorations open() {
        getSelector().open(getOwner().getPlayer());
        return this;
    }

    public ItemStack addTag(ItemStack stack, String name) {
        net.minecraft.server.v1_8_R3.ItemStack nmsCopy = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound nbtTagCompound = (nmsCopy.hasTag()) ? nmsCopy.getTag() : new NBTTagCompound();
        nbtTagCompound.set("celebration", new NBTTagString(name));
        nmsCopy.setTag(nbtTagCompound);
        return CraftItemStack.asBukkitCopy(nmsCopy);
    }

}
