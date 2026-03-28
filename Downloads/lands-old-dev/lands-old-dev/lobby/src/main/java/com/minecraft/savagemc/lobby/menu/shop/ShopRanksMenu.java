package com.minecraft.savagemc.lobby.menu.shop;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.lobby.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;

public class ShopRanksMenu extends Menu {

    private final User user;

    public ShopRanksMenu(Player player, Menu last) {
        super(player, "Ranks", last, 6, 21);

        this.user = (User) User.of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        Account account = user.getAccount();

        List<ShopProduct> products = ShopProduct.getRankProducts(account);

        // Adicionar vidros cinzas decorativos nas bordas
        for (int i = 0; i < 9; i++) {
            addItem(i, Item.of(Material.STAINED_GLASS_PANE, 7, " "));
        }
        for (int i = 45; i < 54; i++) {
            addItem(i, Item.of(Material.STAINED_GLASS_PANE, 7, " "));
        }
        for (int i = 9; i < 45; i += 9) {
            addItem(i, Item.of(Material.STAINED_GLASS_PANE, 7, " "));
        }
        for (int i = 17; i < 54; i += 9) {
            addItem(i, Item.of(Material.STAINED_GLASS_PANE, 7, " "));
        }

        // Indicador verde na parte inferior (categoria selecionada)
        addItem(49, Item.of(Material.STAINED_GLASS_PANE, 5, "§aRanks"));

        // Botão de voltar
        addItem(45, Item.of(Material.ARROW, "§cVoltar")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    if (getLast() != null) {
                        getLast().handle();
                    }
                }));

        if (products.isEmpty()) {
            addErrorButton("§aVocê já possui todos os ranks!");
        } else {
            int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
            int index = 0;
            
            for (ShopProduct product : products) {
                if (index >= slots.length) break;
                
                Item item = product.getData() > 0 
                    ? Item.of(product.getIcon(), product.getData(), product.getName())
                    : Item.of(product.getIcon(), product.getName());
                    
                addItem(slots[index], item
                        .flags(ItemFlag.values())
                        .lore(product.getLore())
                        .click(event -> {
                            sound(MenuSound.PAGINATED);
                            new ShopProductMenu(getPlayer(), product, this).handle();
                        }));
                
                index++;
            }
        }

        display();
    }
}
