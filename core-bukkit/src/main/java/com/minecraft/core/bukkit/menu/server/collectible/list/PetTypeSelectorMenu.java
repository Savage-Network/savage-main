package com.minecraft.core.bukkit.menu.server.collectible.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.controller.list.CollectibleController;
import com.minecraft.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PetTypeSelectorMenu extends Menu {

    private final Account account;
    private final CollectibleController controller = Core.getCollectibleController();

    public PetTypeSelectorMenu(Player player, Menu last) {
        super(player, "Tipos de pets", last, 5);
        this.account = Core.getAccountController().of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        java.util.List<String> petsLore = new java.util.ArrayList<>();
        petsLore.add("§7Escolha um dos bichinhos de estimação");
        petsLore.add("§7disponíveis para você utilizar nos lobbies.");
        petsLore.add("");
        petsLore.add("§fSelecionado:");

        java.util.List<CollectibleOperator> activePets = CollectibleOperator.list(getPlayer()).stream()
                .filter(op -> op.getCategory() == CollectibleCategory.PET)
                .collect(java.util.stream.Collectors.toList());

        if (activePets.isEmpty()) {
            petsLore.add("§7• Nenhum(a)");
        } else {
            for (CollectibleOperator op : activePets) {
                String name = op.getCollectible() != null ? op.getCollectible().getName() : op.getCategory().getName();
                petsLore.add("§a• " + name);
            }
        }

        int petsUnlocked = account.getCollectibles(CollectibleCategory.PET).size();
        int petsTotal = controller.list(CollectibleCategory.PET).size();
        int petsPercent = Util.percentage(petsUnlocked, petsTotal);
        petsLore.add("");
        petsLore.add("§fDesbloqueados: §c" + petsUnlocked + "/§c" + petsTotal + " §8(" + petsPercent + "%)");
        petsLore.add("");
        petsLore.add("§eClique para navegar,");

        addItem(12, Item.of(Material.BONE)
                .name("§aBichos de estimação")
                .lore(petsLore)
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new CollectibleSelectorMenu(getPlayer(), CollectibleCategory.PET, this).handle();
                }));

        java.util.List<String> companionsLore = new java.util.ArrayList<>();
        companionsLore.add("§7Escolha um dos companheiros disponíveis");
        companionsLore.add("§7para você utilizar nos lobbies.");
        companionsLore.add("");
        companionsLore.add("§fSelecionado:");

        java.util.List<CollectibleOperator> activeCompanions = CollectibleOperator.list(getPlayer()).stream()
                .filter(op -> op.getCategory() == CollectibleCategory.COMPANION)
                .collect(java.util.stream.Collectors.toList());

        if (activeCompanions.isEmpty()) {
            companionsLore.add("§7• Nenhum(a)");
        } else {
            for (CollectibleOperator op : activeCompanions) {
                String name = op.getCollectible() != null ? op.getCollectible().getName() : op.getCategory().getName();
                companionsLore.add("§a• " + name);
            }
        }

        int companionsUnlocked = account.getCollectibles(CollectibleCategory.COMPANION).size();
        int companionsTotal = controller.list(CollectibleCategory.COMPANION).size();
        int companionsPercent = Util.percentage(companionsUnlocked, companionsTotal);
        companionsLore.add("");
        companionsLore.add("§fDesbloqueados: §c" + companionsUnlocked + "/§c" + companionsTotal + " §8(" + companionsPercent + "%)");
        companionsLore.add("");
        companionsLore.add("§eClique para navegar,");

        addItem(14, Item.of(Material.SKULL_ITEM, 3)
                .name("§aCompanheiros")
                .lore(companionsLore)
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new CollectibleSelectorMenu(getPlayer(), CollectibleCategory.COMPANION, this).handle();
                }));

        addBackButton();
        addCloseButton();

        display();
    }
}