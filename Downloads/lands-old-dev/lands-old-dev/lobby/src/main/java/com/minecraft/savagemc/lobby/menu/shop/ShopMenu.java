package com.minecraft.savagemc.lobby.menu.shop;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.lobby.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class ShopMenu extends Menu {

    private final User user;

    public ShopMenu(Player player) {
        super(player, "Loja Savage", 6);

        this.user = (User) User.of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        Account account = user.getAccount();

        // Linha 1 (slots 0-8)
        addItem(1, Item.of(Material.EMERALD, "§aRanks")
                .flags(ItemFlag.values())
                .lore("§7Clique para selecionar!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new ShopRanksMenu(getPlayer(), this).handle();
                }));

        addItem(7, Item.of(Material.BOOK, "§aComo pagar? (#0340)")
                .flags(ItemFlag.values())
                .lore(
                        "§7Ao escolher um produto",
                        "§7você receberá um mapa",
                        "§7com o QR Code do §aPIX§7.",
                        "",
                        "§7Basta escanear e aguardar!",
                        "",
                        "§fminecraft:book",
                        "§fNBT: 1 tag(s)"
                )
                .click(event -> {
                    sound(MenuSound.DONE);
                }));

        addItem(8, Item.of(Material.PAPER, "§aMeus pedidos (#0339)")
                .flags(ItemFlag.values())
                .lore(
                        "§eClique para visualizar!",
                        "",
                        "§fminecraft:paper",
                        "§fNBT: 1 tag(s)"
                )
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new ShopOrdersMenu(getPlayer(), this).handle();
                }));

        // Linha 4 (slots 27-35) - BETA, SAVAGE e VIP
        addItem(28, Item.of(Material.INK_SACK, 12, "§b§lBETA")
                .flags(ItemFlag.values())
                .lore(
                        "§7Permanente.",
                        "",
                        "§a✔ §7Voo no Lobby;",
                        "§a✔ §7Selecionar mapas;",
                        "§a✔ §7Acesso à kits extras;",
                        "§a✔ §7Acesso antecipado a modos;",
                        "§a✔ §7Entrar em salas lotadas;",
                        "§a✔ §7Tag §bBETA",
                        "",
                        "§7Preço: §aR$30,00",
                        "",
                        "§eClique para adquirir!"
                )
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new ShopProductMenu(getPlayer(), ShopProduct.BETA, this).handle();
                }));

        addItem(31, Item.of(Material.EMERALD, "§2§lSAVAGE")
                .flags(ItemFlag.values())
                .lore(
                        "§7Permanente.",
                        "",
                        "§a✔ §7Todos os benefícios VIP;",
                        "§a✔ §7Todos os benefícios BETA;",
                        "§a✔ §7Acesso prioritário;",
                        "§a✔ §7Kits exclusivos;",
                        "§a✔ §7Tag §2SAVAGE;",
                        "§7E muito mais!",
                        "",
                        "§7Preço: §aR$27,99",
                        "",
                        "§eClique para adquirir!"
                )
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new ShopProductMenu(getPlayer(), ShopProduct.SAVAGE, this).handle();
                }));

        addItem(34, Item.of(Material.INK_SACK, 10, "§a§lVIP")
                .flags(ItemFlag.values())
                .lore(
                        "§7Permanente.",
                        "",
                        "§a✔ §7Voo no Lobby;",
                        "§a✔ §7Selecionar mapas;",
                        "§a✔ §7Anúncia ao entrar;",
                        "§a✔ §7Preferências VIP;",
                        "§7E muito mais!",
                        "",
                        "§7Preço: §aR$25,00",
                        "",
                        "§eClique para adquirir!"
                )
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new ShopProductMenu(getPlayer(), ShopProduct.VIP, this).handle();
                }));

        // Linha 5 (slots 36-44) - Papéis com benefícios
        addItem(37, Item.of(Material.PAPER, "§b§lBETA")
                .flags(ItemFlag.values())
                .lore(
                        "§7Permanente.",
                        "",
                        "§a✔ §7Voo no Lobby;",
                        "§a✔ §7Selecionar mapas;",
                        "§a✔ §7Acesso à kits extras;",
                        "§a✔ §7Acesso antecipado a modos;",
                        "§a✔ §7Entrar em salas lotadas;",
                        "§a✔ §7Tag §bBETA"
                ));

        addItem(40, Item.of(Material.PAPER, "§2§lSAVAGE")
                .flags(ItemFlag.values())
                .lore(
                        "§7Permanente.",
                        "",
                        "§a✔ §7Todos os benefícios VIP;",
                        "§a✔ §7Todos os benefícios BETA;",
                        "§a✔ §7Acesso prioritário;",
                        "§a✔ §7Kits exclusivos;",
                        "§a✔ §7Tag §2SAVAGE;",
                        "§7E muito mais!"
                ));

        addItem(43, Item.of(Material.PAPER, "§a§lVIP")
                .flags(ItemFlag.values())
                .lore(
                        "§7Permanente.",
                        "",
                        "§a✔ §7Voo no Lobby;",
                        "§a✔ §7Selecionar mapas;",
                        "§a✔ §7Anúncia ao entrar;",
                        "§a✔ §7Preferências VIP;",
                        "§7E muito mais!"
                ));

        display();
    }
}
