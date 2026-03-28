package com.minecraft.savagemc.lobby.shop;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PixQRCodeGenerator {

    private static final String PIX_KEY = "6e993309-08ed-47a7-9e61-8f7e5809b5fe";

    public static ItemStack generatePixMap(Player player, ShopOrder order) {
        String pixPayload = generatePixPayload(order);
        
        ItemStack map = new ItemStack(Material.MAP);
        MapView mapView = Bukkit.createMap(player.getWorld());
        
        for (MapRenderer renderer : mapView.getRenderers()) {
            mapView.removeRenderer(renderer);
        }
        
        mapView.addRenderer(new MapRenderer() {
            private boolean rendered = false;
            
            @Override
            public void render(MapView map, MapCanvas canvas, Player player) {
                if (rendered) return;
                rendered = true;
                
                BufferedImage qrCode = generateQRCodeWithLogo(pixPayload, 128);
                
                if (qrCode != null) {
                    byte[] pixels = imageToMinecraftPixels(qrCode);
                    
                    for (int x = 0; x < 128; x++) {
                        for (int y = 0; y < 128; y++) {
                            canvas.setPixel(x, y, pixels[y * 128 + x]);
                        }
                    }
                }
            }
        });
        
        map.setDurability(mapView.getId());
        return map;
    }

    private static byte[] imageToMinecraftPixels(BufferedImage image) {
        byte[] pixels = new byte[128 * 128];
        
        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                int rgb = image.getRGB(x, y);
                Color color = new Color(rgb);
                pixels[y * 128 + x] = MapPalette.matchColor(color.getRed(), color.getGreen(), color.getBlue());
            }
        }
        
        return pixels;
    }

    private static String generatePixPayload(ShopOrder order) {
        String merchantName = "SAVAGEMC";
        String city = "SAOPAULO";
        String txid = order.getOrderId();
        
        StringBuilder payload = new StringBuilder();
        payload.append(buildTag("00", "01"));
        payload.append(buildTag("01", "12"));
        
        StringBuilder gui = new StringBuilder();
        gui.append(buildTag("00", "br.gov.bcb.pix"));
        gui.append(buildTag("01", PIX_KEY));
        payload.append(buildTag("26", gui.toString()));
        
        payload.append(buildTag("52", "0000"));
        payload.append(buildTag("53", "986"));
        
        String amount = String.format("%.2f", order.getPrice()).replace(",", ".");
        payload.append(buildTag("54", amount));
        
        payload.append(buildTag("58", "BR"));
        payload.append(buildTag("59", merchantName));
        payload.append(buildTag("60", city));
        
        StringBuilder additionalData = new StringBuilder();
        additionalData.append(buildTag("05", txid));
        payload.append(buildTag("62", additionalData.toString()));
        
        payload.append("6304");
        String crc = calculateCRC16(payload.toString());
        payload.append(crc);
        
        return payload.toString();
    }
    
    private static String buildTag(String id, String value) {
        return id + String.format("%02d", value.length()) + value;
    }

    private static String calculateCRC16(String data) {
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

    private static BufferedImage generateQRCodeWithLogo(String data, int size) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 0);
            
            // QR Code 90x90 para deixar espaço nas bordas
            int qrSize = 90;
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, qrSize, qrSize, hints);
            
            BufferedImage finalImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = finalImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Carregar e desenhar fundo
            try {
                BufferedImage background = ImageIO.read(new File("image.png"));
                if (background != null) {
                    g.drawImage(background, 0, 0, size, size, null);
                } else {
                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, size, size);
                }
            } catch (Exception e) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, size, size);
            }
            
            // Posição centralizada do QR Code
            int qrX = (size - qrSize) / 2;
            int qrY = (size - qrSize) / 2;
            
            // Fundo branco sólido para o QR Code
            g.setColor(Color.WHITE);
            g.fillRect(qrX - 5, qrY - 5, qrSize + 10, qrSize + 10);
            
            // Borda preta dupla
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawRect(qrX - 5, qrY - 5, qrSize + 10, qrSize + 10);
            
            // Desenhar QR Code
            g.setColor(Color.BLACK);
            for (int x = 0; x < qrSize; x++) {
                for (int y = 0; y < qrSize; y++) {
                    if (bitMatrix.get(x, y)) {
                        g.fillRect(qrX + x, qrY + y, 1, 1);
                    }
                }
            }
            
            // Logo no centro
            int logoSize = qrSize / 5;
            int logoX = qrX + (qrSize - logoSize) / 2;
            int logoY = qrY + (qrSize - logoSize) / 2;
            
            g.setColor(Color.WHITE);
            g.fillRoundRect(logoX - 2, logoY - 2, logoSize + 4, logoSize + 4, 4, 4);
            
            try {
                BufferedImage logo = ImageIO.read(new File("image.png"));
                if (logo != null) {
                    g.drawImage(logo, logoX, logoY, logoSize, logoSize, null);
                }
            } catch (Exception e) {
                g.setColor(new Color(0, 200, 0));
                g.fillRoundRect(logoX, logoY, logoSize, logoSize, 3, 3);
                
                g.setColor(Color.WHITE);
                Font font = new Font("Arial", Font.BOLD, logoSize / 2);
                g.setFont(font);
                FontMetrics fm = g.getFontMetrics();
                int textX = logoX + (logoSize - fm.stringWidth("S")) / 2;
                int textY = logoY + ((logoSize - fm.getHeight()) / 2) + fm.getAscent();
                g.drawString("S", textX, textY);
            }
            
            g.dispose();
            return finalImage;
            
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
