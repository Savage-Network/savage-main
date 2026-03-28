package com.minecraft.savagemc.arcade.aquatic.menu.perk;

import com.minecraft.savagemc.arcade.aquatic.controller.PerkController;
import com.minecraft.savagemc.arcade.aquatic.perk.Perk;
import com.minecraft.savagemc.arcade.aquatic.user.User;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PerkMenu extends Menu {

    private final User user;

    public PerkMenu(Player player) {
        super(player, "Selecionar perk", 5, 21);

        this.user = (User) User.of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        Account account = user.getAccount();

        List<Perk> list = new ArrayList<>(PerkController.getPerkList())
                .stream()
                .sorted(Comparator.comparing(Perk::getName))
                .collect(Collectors.toList());

        if (list.isEmpty())
            addErrorButton("§cNão há perks disponíveis...");
        else {
            buildPageItems(list, 10, (perk, slot) -> {
                boolean has = perk.has(account),
                        isUsing = user.isUsingPerk(perk.getName());

                List<String> lore = new ArrayList<>(perk.getLore());

                lore.addAll(Arrays.asList(
                        "",
                        !has ? "§cVocê não possui essa perk."
                                : isUsing ? "§cSelecionado."
                                : "§eClique para selecionar!"
                ));

                addItem(slot, perk.getIcon().clone()
                        .name(((!has || isUsing) ? "§c" : "§a") + perk.getName())
                        .flags(ItemFlag.values())
                        .lore(lore)
                        .click(event -> {

                            if (!has || isUsing) {
                                sound(MenuSound.ERROR);
                                return;
                            }

                            close();
                            sound(MenuSound.DONE);

                            getPlayer().performCommand("perk " + perk.getName());
                        }));
            });
        }

        addCloseButton();

        display();
    }
}
