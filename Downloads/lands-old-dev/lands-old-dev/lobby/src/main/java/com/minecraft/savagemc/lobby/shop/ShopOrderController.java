package com.minecraft.savagemc.lobby.shop;

import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class ShopOrderController {

    @Getter
    private static final ShopOrderController instance = new ShopOrderController();

    private final Map<String, ShopOrder> orders = new HashMap<>();
    private final Map<UUID, List<String>> playerOrders = new HashMap<>();

    private ShopOrderController() {
    }

    public ShopOrder createOrder(UUID playerId, String playerName, String productId, String productName, double price, String pixKey) {
        String orderId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        ShopOrder order = new ShopOrder(
                orderId,
                playerId,
                playerName,
                productId,
                productName,
                price,
                ShopOrder.OrderStatus.PENDING,
                System.currentTimeMillis(),
                pixKey
        );

        orders.put(orderId, order);
        playerOrders.computeIfAbsent(playerId, k -> new ArrayList<>()).add(orderId);

        return order;
    }

    public List<ShopOrder> getPlayerOrders(UUID playerId) {
        List<String> orderIds = playerOrders.getOrDefault(playerId, new ArrayList<>());
        return orderIds.stream()
                .map(orders::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(ShopOrder::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public ShopOrder getOrder(String orderId) {
        return orders.get(orderId);
    }

    public void markAsPaid(String orderId) {
        ShopOrder order = orders.get(orderId);
        if (order != null && order.getStatus() == ShopOrder.OrderStatus.PENDING) {
            order.setStatus(ShopOrder.OrderStatus.PAID);
            
            // Executar comando para dar o rank
            String rankName = getRankNameFromProduct(order.getProductId());
            if (rankName != null) {
                String command = "acc " + order.getPlayerName() + " rank set " + rankName;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                
                // Notificar jogador se estiver online
                if (Bukkit.getPlayer(order.getPlayerId()) != null) {
                    Bukkit.getPlayer(order.getPlayerId()).sendMessage("§a§lPAGAMENTO CONFIRMADO!");
                    Bukkit.getPlayer(order.getPlayerId()).sendMessage("§7Seu " + order.getProductName() + " §7foi ativado!");
                }
            }
        }
    }

    private String getRankNameFromProduct(String productId) {
        switch (productId) {
            case "#0352":
                return "Beta";
            case "#0351":
                return "Vip";
            case "#0353":
                return "Savage";
            default:
                return null;
        }
    }

    public void cancelOrder(String orderId) {
        ShopOrder order = orders.get(orderId);
        if (order != null) {
            order.setStatus(ShopOrder.OrderStatus.CANCELLED);
        }
    }
}
