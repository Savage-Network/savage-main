package com.minecraft.savagemc.lobby.menu.shop;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.lobby.shop.PixQRCodeGenerator;
import com.minecraft.savagemc.lobby.shop.ShopOrder;
import com.minecraft.savagemc.lobby.shop.ShopOrderController;
import com.minecraft.savagemc.lobby.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class ShopProductMenu extends Menu {

    private final User user;
    private final ShopProduct product;

    public ShopProductMenu(Player player, ShopProduct product, Menu last) {
        super(player, "Compra: " + product.getName(), last, 1);

        this.user = (User) User.of(player.getUniqueId());
        this.product = product;
    }

    @Override
    public void handle() {
        clear();

        Account account = user.getAccount();

        // Botão PIX
        addItem(4, Item.of(Material.INK_SACK, 10, "§aPagar com Pix")
                .flags(ItemFlag.values())
                .lore(
                        "§7Clique para gerar o",
                        "§7QR Code do pagamento.",
                        "",
                        "§7Produto: " + product.getName(),
                        "§7Preço: §aR$ " + String.format("%.2f", product.getPrice()),
                        "",
                        "§eClique para pagar!",
                        "",
                        "§fminecraft:dye",
                        "§fNBT: 1 tag(s)"
                )
                .click(event -> {
                    sound(MenuSound.DONE);
                    getPlayer().closeInventory();
                    
                    // Chave PIX
                    String pixKey = "6e993309-08ed-47a7-9e61-8f7e5809b5fe";
                    
                    // Criar pedido
                    ShopOrder order = ShopOrderController.getInstance().createOrder(
                            getPlayer().getUniqueId(),
                            getPlayer().getName(),
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            pixKey
                    );
                    
                    // Gerar mapa com QR Code PIX
                    ItemStack map = PixQRCodeGenerator.generatePixMap(getPlayer(), order);
                    
                    getPlayer().getInventory().addItem(map);
                    getPlayer().sendMessage("§aVocê recebeu o QR Code PIX no seu inventário!");
                    getPlayer().sendMessage("§7Pedido: §e#" + order.getOrderId());
                    getPlayer().sendMessage("§7Escaneie o código para realizar o pagamento.");
                    getPlayer().sendMessage("§7Chave PIX: §f" + pixKey);
                }));

        display();
    }
}
