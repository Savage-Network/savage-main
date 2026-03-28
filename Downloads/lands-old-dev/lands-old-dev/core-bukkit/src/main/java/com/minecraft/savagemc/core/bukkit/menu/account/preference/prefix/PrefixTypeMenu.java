package com.minecraft.savagemc.core.bukkit.menu.account.preference.prefix;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.Rank;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.account.context.objects.tag.prefix.TagPrefix;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrefixTypeMenu extends Menu {

    private final Account account;

    public PrefixTypeMenu(Player player, Account account, Menu last) {
        super(player, "Estilo de prefixo", last, 3);

        this.account = account;
    }

    @Override
    public void handle() {
        clear();

        int slot = 10;
        for (TagPrefix prefix : TagPrefix.list()) {
            boolean using = account.isUsingTagPrefix(prefix);

            Item icon = Item.of(Material.INK_SACK, prefix.getColorId(), (using ? "§c" : "§a") + prefix.getName())
                    .flags(ItemFlag.values());

            if (using)
                icon.enchantment(Enchantment.DURABILITY, 1);

            List<String> lore = new ArrayList<>(Arrays.asList(
                    "",
                    "§7Prévias:"
            ));

            for (Rank rank : account.getAllRanks()) {
                if (rank.getType().equals(RankType.MEMBER)) continue;

                Tag tag = Tag.of(rank.getType());

                lore.add(tag.getByPrefix(prefix) + account.getNickname());
            }

            lore.addAll(Arrays.asList(
                    "",
                    account.isUsingTagPrefix(prefix) ? "§cJá selecionado." : "§eClique para selecionar!"
            ));

            addItem(slot, icon.lore(lore)
                    .click(event -> {
                        if (using) {
                            sound(MenuSound.ERROR);
                            return;
                        }

                        close();
                        sound(MenuSound.DONE);

                        getPlayer().performCommand("prefixtype " + prefix.name());
                    }));

            slot++;
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
