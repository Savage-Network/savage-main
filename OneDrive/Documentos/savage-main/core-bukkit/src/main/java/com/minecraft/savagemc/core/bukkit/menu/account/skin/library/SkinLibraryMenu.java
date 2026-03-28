package com.minecraft.savagemc.core.bukkit.menu.account.skin.library;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.skin.Skin;
import com.minecraft.savagemc.core.api.skin.objects.SkinCategory;
import com.minecraft.savagemc.core.api.skin.library.SkinLibrary;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.savagemc.core.util.list.DateUtil;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

@Setter
public class SkinLibraryMenu extends Menu {

    private final Account account;
    private final SkinCategory category;

    public SkinLibraryMenu(Player player, SkinCategory category, Menu last) {
        super(player, "Biblioteca - " + category.getName(), last, 5, 21);

        this.account = Savage.getAccountController().of(player.getUniqueId());
        this.category = category;
    }

    @Override
    public void handle() {
        clear();

        List<SkinLibrary> list = SkinLibrary.getLibrarySkins(category);

        if (list.isEmpty())
            addItem(22, Item.of(Material.WEB, "§cNão há skins..."));
        else {
            // Construir páginas
            buildPageItems(list, 10, (library, slot) -> {
                Skin skin = library.getSkin();

                boolean using = account.hasSkin(skin);

                addItem(slot, Item.of(Material.SKULL_ITEM, 3, "§a" + skin.getDisplayName(),
                                "§8" + library.getGender().getName(),
                                "",
                                "§7Coleção: " + library.getCollection().getColoredName(),
                                "§7Postado em: " + DateUtil.getSimpleDate(library.getReleasedAt()),
                                "",
                                !using ? "§eClique para escolher!" : "§cJá escolhida.")
                        .skullByBase64(skin.getValue())
                        .click(event -> {
                            if (using) {
                                sound(MenuSound.ERROR);

                                account.send("§cVocê já está usando a skin " + skin.getDisplayName() + ".");
                                return;
                            }

                            close();
                            sound(MenuSound.SUCCESS);

                            account.setSkin(skin);
                            ProtocolHandler.changePlayerSkin(getPlayer(), skin);

                            account.send("§aVocê selecionou a skin §7" + skin.getDisplayName() + "§a.");
                        }));
            });
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
