package com.minecraft.savagemc.core.bukkit.menu.account.skin;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.skin.Skin;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.core.bukkit.menu.account.skin.library.SkinLibraryCategoryMenu;
import com.minecraft.savagemc.core.util.list.DateUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SkinInfoMenu extends Menu {

    public SkinInfoMenu(Player player, Menu last) {
        super(player, "Skin", last, 6);
    }

    @Override
    public void handle() {
        clear();

        Account account = Savage.getAccountController().of(getPlayer().getUniqueId());

        Skin skin = account.getSkin();

        boolean isDefaultSkin = account.isPremium() && account.isUsingProfileSkin();

        addItem(13, Item.of(Material.SKULL_ITEM, 3, "§a" + skin.getDisplayName(),
                        "§7Fonte: §f" + skin.getType().getName(),
                        "§7Modificado em: §f" + DateUtil.getDate(account.getSkinMetadata().getUpdatedAt()))
                .skullByBase64(skin.getValue()));

        addItem(29 + (isDefaultSkin ? 1 : 0), Item.of(Material.BOOKSHELF, "§aBiblioteca de skins",
                        "§7Confira o pacote de skins",
                        "§7gratuitas para você usar.",
                        "",
                        "§eClique para ver!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);

                    new SkinLibraryCategoryMenu(getPlayer(), this).handle();
                }));

        if (!isDefaultSkin)
            addItem(31, Item.of(Material.BARRIER, "§cRestaurar Skin",
                            "§7Volte para a sua skin padrão.",
                            "",
                            "§eClique para restaurar!")
                    .click(event -> {
                        close();
                        sound(MenuSound.SUCCESS);

                        getPlayer().performCommand("skin #");
                    }));

        addItem(33 - (isDefaultSkin ? 1 : 0), Item.of(Material.ANVIL, "§aModificar Skin",
                        "§7Customize a sua skin",
                        "§7do seu jeito.",
                        "",
                        "§7Exclusivo para " + RankType.VIP.getBoldColorName().toUpperCase(),
                        "",
                        "§eClique para customizar!")
                .click(event -> {
                    close();

                    if (!account.isVIP()) {
                        sound(MenuSound.ERROR);

                        account.send("§cVocê não pode customizar sua skin! ;(",
                                "§cAdquira o rank " + RankType.VIP.getColoredName() + "§c em: §e" + Constant.SERVER_STORE);
                        return;
                    }
                    sound(MenuSound.SUCCESS);

                    TextComponent text = new TextComponent("§6§lSKIN §eCustomize a sua §6skin§e usando: §7/skin (nome)§e, ou clique ");

                    TextComponent button = new TextComponent("§a§lAQUI");

                    button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para customizar a sua skin.")));
                    button.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/skin (nome)"));

                    text.addExtra(button);
                    text.addExtra("§e.");

                    account.send(text);
                }));

        if (isReturnable())
            addBackButton();

        display();
    }
}
