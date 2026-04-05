package com.minecraft.savagemc.arcade.bedwars.menu.shop;

import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.core.member.list.bedwars.BedMember;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.BedShop;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import com.minecraft.savagemc.core.member.list.bedwars.objects.menu.item.BedItem;
import com.minecraft.savagemc.core.member.list.bedwars.objects.menu.type.BedMenuType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FavoriteShopMenu extends Menu {

    private final Arena arena;

    private final BedMember member;
    private final BedWarsItem item;

    public FavoriteShopMenu(Player player, BedWarsItem item, Menu last) {
        super(player, "Selecionando espaço...", last, 6, 21);

        User user = (User) User.of(player.getUniqueId());

        this.arena = user.getArena();
        this.member = user.getMember();

        this.item = item;
    }

    @Override
    public void handle() {
        clear();

        List<BedItem> list = new ArrayList<>();

        // Verificar se o menu de favoritos existe
        if (member.getMenu(BedMenuType.FAVORITE) != null) {
            list = member.getMenu(BedMenuType.FAVORITE).getItemList();
        }

        /* Adicionando item selecionado */
        List<String> lore = new ArrayList<>();

        lore.add("");
        lore.addAll(item.getLore());
        lore.addAll(Arrays.asList("", "§eAdicionando..."));

        addItem(4, Item.fromStack(item.getStack())
                .name("§c" + item.getName())
                .lore(lore));

        // Adicionar vidros cinzas apenas na linha 2
        for (int i = 9; i < 18; i++) {
            addItem(i, Item.of(Material.STAINED_GLASS_PANE, 7, " "));
        }

        /* Adicionando Espaços Vázios - 3 linhas de 7 slots (21 slots totais) */
        // Linhas 3-5 (slots 19-25, 28-34, 37-43)
        int[] slots = {
            19, 20, 21, 22, 23, 24, 25,  // Linha 3
            28, 29, 30, 31, 32, 33, 34,  // Linha 4
            37, 38, 39, 40, 41, 42, 43   // Linha 5
        };

        for (int slot : slots) {
            BedItem existingItem = list.stream().filter(root -> root.getSlot() == slot).findFirst().orElse(null);

            if (existingItem != null) {
                BedWarsItem root = BedWarsItem.of(existingItem.getName());

                if (root != null && !root.isAvailableForMode(arena.getType())) {
                    addItem(slot, buildIncompatibleItem());
                } else {
                    // Mostrar o item que já está favoritado com opção de remover
                    if (root != null) {
                        List<String> itemLore = new ArrayList<>(root.getLore());
                        itemLore.addAll(Arrays.asList(
                                "",
                                "§eClique esquerdo§7 para substituir.",
                                "§eClique direito§7 para remover."
                        ));
                        
                        addItem(slot, Item.fromStack(root.getStack())
                                .name("§a" + root.getName())
                                .lore(itemLore)
                                .click(event -> {
                                    if (event.isRightClick()) {
                                        // Remover item favorito
                                        sound(MenuSound.DONE);
                                        member.removeFavoriteItem(root);
                                        getPlayer().sendMessage("§cItem §6" + root.getName() + " §cremovido dos favoritos!");
                                        handle();
                                    } else if (event.isLeftClick()) {
                                        // Substituir item
                                        member.setFavoriteItem(slot, item);
                                        sound(MenuSound.DONE);
                                        getPlayer().sendMessage("§aItem §6" + item.getName() + " §aadicionado aos favoritos!");
                                        new ShopMenu(getPlayer(), BedShop.FAVORITE).handle();
                                    }
                                }));
                    } else {
                        addItem(slot, buildEmptySpace(slot));
                    }
                }
            } else {
                addItem(slot, buildEmptySpace(slot));
            }
        }
        
        // Adicionar vidros cinzas nas laterais das linhas 3-5
        int[] borderSlots = {18, 26, 27, 35, 36, 44};
        for (int slot : borderSlots) {
            addItem(slot, Item.of(Material.STAINED_GLASS_PANE, 7, " "));
        }
        
        // Adicionar botão do editor de hotbar no último slot (slot 53)
        addItem(53, Item.of(Material.BLAZE_POWDER)
                .name("§eEditor de hotbar (&#0381) (&#0372)")
                .lore("§7Modifique previamente slots",
                        "§7preferidos ao comprar um item.",
                        "",
                        "§eClique para editar!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new HotbarEditorMenu(getPlayer(), this).handle();
                }));

        if (isReturnable())
            addBackButton(45);

        display();
    }

    protected Item buildEmptySpace(int slot) {
        return Item.of(Material.STAINED_GLASS_PANE, 14, "§cEspaço vázio!",
                        "§eClique para selecionar!")
                .click(event -> {
                    member.setFavoriteItem(slot, item);

                    sound(MenuSound.DONE);

                    getPlayer().sendMessage("§aItem §6" + item.getName() + " §aadicionado aos favoritos!");

                    new ShopMenu(getPlayer(), BedShop.FAVORITE).handle();
                });
    }

    protected Item buildIncompatibleItem() {
        return Item.of(Material.STAINED_GLASS_PANE, 14, "§cItem incompatível!",
                "§7Este espaço está ocupado",
                "§7por um item incompatível.");
    }
}
