package com.minecraft.savagemc.core.bukkit.menu.account;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.core.bukkit.menu.account.friend.FriendMenu;
import com.minecraft.savagemc.core.bukkit.menu.account.skin.SkinInfoMenu;
import com.minecraft.savagemc.core.bukkit.menu.account.stats.StatsMenu;
import com.minecraft.savagemc.core.bukkit.menu.account.preference.PreferenceMenu;
import com.minecraft.savagemc.core.bukkit.menu.server.clan.ClanMenu;
import com.minecraft.savagemc.core.util.list.DateUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AccountMenu extends Menu {

    private final Account account;

    public AccountMenu(Player player) {
        super(player, "Minha Conta", 6);

        this.account = Savage.getAccountController().of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        addItem(13, Item.of(Material.SKULL_ITEM, 3, "§a" + account.getName(),
                        "§7Rank: " + account.getRank().getColoredName(),
                        "§7Primeira entrada: " + DateUtil.getSimpleDate(account.getContext().getCreatedAt()),
                        "§7Última entrada: " + DateUtil.getSimpleDate(account.getContext().getLastLogin()))
                .skullByBase64(account.getSkin().getValue()));

        addItem(29, Item.of(Material.ITEM_FRAME, "§aSkin",
                        "§7Confira diversas skins, e",
                        "§7customize do seu jeito.",
                        "",
                        "§eClique para ver!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new SkinInfoMenu(getPlayer(), this).handle();
                }));

        addItem(30, Item.of(Material.PAPER, "§aEstatísticas",
                        "§7Veja o seu desempenho",
                        "§7no servidor geral.",
                        "",
                        "§eClique para ver!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new StatsMenu(getPlayer(), account, this).handle();
                }));

        addItem(31, Item.of(Material.DIODE, "§aPreferências",
                        "§7Customize a sua experiência",
                        "§7em nossos servidores.",
                        "",
                        "§eClique para editar!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new PreferenceMenu(getPlayer(), this).handle();
                }));

        addItem(32, Item.of(Material.COOKIE, "§aAmigos",
                        "§7Confira os seus amigos,",
                        "§7e faça novas amizades.",
                        "",
                        "§eClique para ver!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new FriendMenu(getPlayer(), this).handle();
                }));

        addItem(33, Item.of(Material.ANVIL, "§aClan",
                        "§7Confira informações sobre",
                        "§7o seu clan atual.",
                        "",
                        "§eClique para ver!")
                .click(event -> {
                    if (!account.hasClan()) {
                        sound(MenuSound.ERROR);
                        account.send("§cVocê ainda não faz parte de um clan!");
                        return;
                    }

                    sound(MenuSound.PAGINATED);
                    new ClanMenu(getPlayer(), account.getClan(), this).handle();
                }));

        display();
    }
}
