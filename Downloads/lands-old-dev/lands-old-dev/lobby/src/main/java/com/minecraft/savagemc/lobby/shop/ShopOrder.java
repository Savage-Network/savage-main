package com.minecraft.savagemc.lobby.shop;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ShopOrder {

    private String orderId;
    private UUID playerId;
    private String playerName;
    private String productId;
    private String productName;
    private double price;
    private OrderStatus status;
    private long createdAt;
    private String pixKey;

    public enum OrderStatus {
        PENDING("§ePendente", "§e"),
        PAID("§aPago", "§a"),
        CANCELLED("§cCancelado", "§c");

        @Getter
        private final String displayName;
        @Getter
        private final String color;

        OrderStatus(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
    }
}
