package com.minecraft.savagemc.core.bukkit.menu.account.skin.library;

import com.minecraft.savagemc.core.api.skin.Skin;
import com.minecraft.savagemc.core.api.skin.objects.SkinCategory;
import com.minecraft.savagemc.core.api.skin.library.SkinLibrary;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SkinLibraryCategoryMenu extends Menu {

    public SkinLibraryCategoryMenu(Player player, Menu last) {
        super(player, "Biblioteca de skins", last, 4);
    }

    @Override
    public void handle() {
        clear();

        buildCategory(11, SkinCategory.CHARACTER);
        buildCategory(15, SkinCategory.FESTIVE);

        if (isReturnable())
            addBackButton(getTotalSlots() - 6);

        display();
    }

    protected void buildCategory(int slot, SkinCategory category) {
        Skin first = SkinLibrary.getSkins(category).get(0);

        addItem(slot, Item.of(Material.SKULL_ITEM, 3, "§a" + category.getName(),
                "§eClique para ver!")
                .skullByBase64(first.getValue())
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new SkinLibraryMenu(getPlayer(), category, this).handle();
                }));
    }
}
