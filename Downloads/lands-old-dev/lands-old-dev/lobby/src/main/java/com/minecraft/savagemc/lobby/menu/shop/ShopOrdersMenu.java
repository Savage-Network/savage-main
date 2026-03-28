package com.minecraft.savagemc.lobby.menu.shop;

import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.lobby.shop.ShopOrder;
import com.minecraft.savagemc.lobby.shop.ShopOrderController;
import com.minecraft.savagemc.lobby.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShopOrdersMenu extends Menu {

    private final User user;

    public ShopOrdersMenu(Player player, Menu last) {
        super(player, "Meus pedidos", last, 6, 21);

        this.user = (User) User.of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        // Botão de voltar
        addItem(4, Item.of(Material.ARROW, "§cVoltar")
                .flags(ItemFlag.values())
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    if (getLast() != null) {
                        getLast().handle();
                    }
                }));

        List<ShopOrder> orders = ShopOrderController.getInstance().getPlayerOrders(getPlayer().getUniqueId());

        if (orders.isEmpty()) {
            addErrorButton("§7Você não possui pedidos pendentes.");
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            int slot = 19;
            for (ShopOrder order : orders) {
                if (slot > 34) break; // Limite de slots
                
                Material material;
                int data = 0;
                
                // Definir ícone baseado no produto
                switch (order.getProductId()) {
                    case "#0352": // BETA
                        material = Material.INK_SACK;
                        data = 12;
                        break;
                    case "#0351": // VIP
                        material = Material.INK_SACK;
                        data = 10;
                        break;
                    case "#0353": // SAVAGE
                        material = Material.EMERALD;
                        break;
                    default:
                        material = Material.PAPER;
                        break;
                }
                
                List<String> lore = new ArrayList<>();
                lore.add("§7Pedido: §e#" + order.getOrderId());
                lore.add("§7Produto: " + order.getProductName());
                lore.add("§7Valor: §aR$ " + String.format("%.2f", order.getPrice()));
                lore.add("§7Status: " + order.getStatus().getDisplayName());
                lore.add("§7Data: §f" + dateFormat.format(new Date(order.getCreatedAt())));
                lore.add("");
                
                if (order.getStatus() == ShopOrder.OrderStatus.PENDING) {
                    lore.add("§7Aguardando pagamento...");
                } else if (order.getStatus() == ShopOrder.OrderStatus.PAID) {
                    lore.add("§aPagamento confirmado!");
                }
                
                Item item;
                if (material == Material.INK_SACK) {
                    item = Item.of(material, data, order.getProductName())
                            .flags(ItemFlag.values())
                            .lore(lore.toArray(new String[0]));
                } else {
                    item = Item.of(material, order.getProductName())
                            .flags(ItemFlag.values())
                            .lore(lore.toArray(new String[0]));
                }
                
                addItem(slot, item);
                slot++;
            }
        }

        display();
    }
}

