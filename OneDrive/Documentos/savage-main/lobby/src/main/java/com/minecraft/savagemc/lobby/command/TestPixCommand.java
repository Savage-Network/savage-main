package com.minecraft.savagemc.lobby.command;

import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.lobby.shop.ShopOrder;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TestPixCommand implements CommandInheritor {

    @Command(name = "testpix", rank = RankType.ADMIN)
    public void testPix(BukkitCommandContext context) {
        Player player = context.getPlayer();
        
        // Criar pedido de teste
        ShopOrder testOrder = new ShopOrder(
                "TEST1234",
                player.getUniqueId(),
                player.getName(),
                "#0352",
                "§b§lBETA",
                30.00,
                ShopOrder.OrderStatus.PENDING,
                System.currentTimeMillis(),
                "6e993309-08ed-47a7-9e61-8f7e5809b5fe"
        );
        
        // Gerar payload PIX
        String payload = generatePixPayload(testOrder);
        
        player.sendMessage("§aPayload PIX gerado:");
        player.sendMessage("§f" + payload);
        player.sendMessage("§7Copie e cole em um validador PIX online para testar.");
    }
    
    private String generatePixPayload(ShopOrder order) {
        String PIX_KEY = "6e993309-08ed-47a7-9e61-8f7e5809b5fe";
        String merchantName = "SAVAGEMC";
        String city = "SAOPAULO";
        String txid = order.getOrderId();
        
        StringBuilder payload = new StringBuilder();
        
        payload.append("000201");
        payload.append("010212");
        
        StringBuilder merchantAccount = new StringBuilder();
        merchantAccount.append("0014br.gov.bcb.pix");
        merchantAccount.append("01").append(String.format("%02d", PIX_KEY.length())).append(PIX_KEY);
        
        payload.append("26").append(String.format("%02d", merchantAccount.length())).append(merchantAccount);
        payload.append("52040000");
        payload.append("5303986");
        
        String amount = String.format("%.2f", order.getPrice()).replace(",", ".");
        payload.append("54").append(String.format("%02d", amount.length())).append(amount);
        
        payload.append("5802BR");
        payload.append("59").append(String.format("%02d", merchantName.length())).append(merchantName);
        payload.append("60").append(String.format("%02d", city.length())).append(city);
        
        StringBuilder additionalData = new StringBuilder();
        additionalData.append("05").append(String.format("%02d", txid.length())).append(txid);
        
        payload.append("62").append(String.format("%02d", additionalData.length())).append(additionalData);
        payload.append("6304");
        
        String crc = calculateCRC16(payload.toString());
        payload.append(crc);
        
        return payload.toString();
    }
    
    private String calculateCRC16(String data) {
        int crc = 0xFFFF;
        byte[] bytes = data.getBytes();
        
        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc = crc << 1;
                }
            }
        }
        
        crc &= 0xFFFF;
        return String.format("%04X", crc);
    }
}
