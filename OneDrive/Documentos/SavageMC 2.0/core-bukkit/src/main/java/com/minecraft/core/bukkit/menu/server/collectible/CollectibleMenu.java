package com.minecraft.core.bukkit.menu.server.collectible;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.menu.server.collectible.list.CollectibleSelectorMenu;
import com.minecraft.core.util.Util;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.controller.list.CollectibleController;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class CollectibleMenu extends Menu {

    private final Account account;
    private final CollectibleController controller = Core.getCollectibleController();

    public CollectibleMenu(Player player) {
        super(player, "Cosméticos", 5);

        this.account = Core.getAccountController().of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        // Linha 2 (slots 11-15) - Primeira linha de categorias
        addCategoryItem(11, Material.COMMAND, "§aArtefatos", CollectibleCategory.ARTIFACT,
                "§7Divirta-se por nossos lobbies",
                "§7utilizando um de nossos artefatos.");

        addCategoryItem(13, Material.BLAZE_POWDER, "§cPartículas", CollectibleCategory.PARTICLE,
                "§7Selecione uma partícula",
                "§7para se destacar.");

        addCategoryItem(15, Material.NETHER_STAR, "§cCapas", CollectibleCategory.CAPES,
                "§7Escolha uma dentre as diversas capas",
                "§7disponíveis para você utilizar nos lobbies.");

        // Linha 3 (slots 20-22) - Segunda linha de categorias
        addCategoryItem(20, Material.NAME_TAG, "§aTítulos", CollectibleCategory.TITLE,
                "§7Selecione um título",
                "§7para esbanjar exclusividade.");

        addCategoryItem(22, Material.BOOK, "§aMensagens de Entrada", CollectibleCategory.JOIN_MESSAGE,
                "§7Personalize sua mensagem",
                "§7de entrada nos lobbies.");

        addCloseButton(40);

        display();
    }

    private Item addCategoryItem(int slot, Material material, String name, CollectibleCategory category, String... description) {
        List<String> lore = new ArrayList<>(Arrays.asList(description));
        lore.add("");
        lore.add("§fSelecionado:");

        List<CollectibleOperator> active = CollectibleOperator.list(getPlayer()).stream()
                .filter(op -> op.getCategory() == category)
                .collect(Collectors.toList());

        if (active.isEmpty()) {
            lore.add("§7• Nenhum(a)");
        } else {
            for (CollectibleOperator op : active) {
                String collectibleName = op.getCollectible() != null ? op.getCollectible().getName() : op.getCategory().getName();
                lore.add("§a• " + collectibleName);
            }
        }

        lore.add("");
        int unlocked = account.getCollectibles(category).size();
        int total = controller.list(category).size();
        int percent = Util.percentage(unlocked, total);
        lore.add("§fDesbloqueados: §c" + unlocked + "/§c" + total + " §8(" + percent + "%)");
        lore.add("");
        lore.add("§eClique para navegar!");

        Item item = Item.of(material)
                .name(name)
                .lore(lore)
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new CollectibleSelectorMenu(getPlayer(), category, this).handle();
                });

        addItem(slot, item);
        return item;
    }

}
