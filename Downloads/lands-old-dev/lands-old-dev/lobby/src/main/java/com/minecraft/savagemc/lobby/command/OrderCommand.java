package com.minecraft.savagemc.lobby.command;

import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.lobby.shop.ShopOrder;
import com.minecraft.savagemc.lobby.shop.ShopOrderController;
import org.bukkit.entity.Player;

public class OrderCommand implements CommandInheritor {

    @Command(name = "pedido", aliases = {"order"}, rank = RankType.ADMIN)
    public void order(BukkitCommandContext context) {
        Player player = context.getPlayer();
        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage("§cUso: /pedido <pagar|cancelar|listar> [id]");
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "pagar":
            case "pay":
                if (args.length < 2) {
                    player.sendMessage("§cUso: /pedido pagar <id>");
                    return;
                }
                String payId = args[1].toUpperCase();
                ShopOrder payOrder = ShopOrderController.getInstance().getOrder(payId);
                if (payOrder == null) {
                    player.sendMessage("§cPedido não encontrado!");
                } else {
                    ShopOrderController.getInstance().markAsPaid(payId);
                    player.sendMessage("§aPedido §e#" + payId + " §amarcado como pago!");
                }
                break;

            case "cancelar":
            case "cancel":
                if (args.length < 2) {
                    player.sendMessage("§cUso: /pedido cancelar <id>");
                    return;
                }
                String cancelId = args[1].toUpperCase();
                ShopOrder cancelOrder = ShopOrderController.getInstance().getOrder(cancelId);
                if (cancelOrder == null) {
                    player.sendMessage("§cPedido não encontrado!");
                } else {
                    ShopOrderController.getInstance().cancelOrder(cancelId);
                    player.sendMessage("§aPedido §e#" + cancelId + " §acancelado!");
                }
                break;

            case "listar":
            case "list":
                var allOrders = ShopOrderController.getInstance().getOrders();
                if (allOrders.isEmpty()) {
                    player.sendMessage("§cNenhum pedido cadastrado!");
                } else {
                    player.sendMessage("§aPedidos cadastrados: §f" + allOrders.size());
                    allOrders.values().forEach(order -> 
                        player.sendMessage("§e#" + order.getOrderId() + " §7- " + order.getPlayerName() + 
                                " §7- " + order.getProductName() + " §7- " + order.getStatus().getDisplayName())
                    );
                }
                break;

            default:
                player.sendMessage("§cUso: /pedido <pagar|cancelar|listar> [id]");
                break;
        }
    }
}
