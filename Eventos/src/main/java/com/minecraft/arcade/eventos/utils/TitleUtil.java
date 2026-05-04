package com.minecraft.arcade.eventos.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Títulos na tela (1.8 NMS via reflexão). Cores via & são convertidas —
 * mesmo padrão usado nos textos Savage (economiza § nos configs YAML).
 */
public final class TitleUtil {

    private static boolean nmsDisponivel = true;
    private static Method getHandle;
    private static Method sendPacket;
    private static Constructor<?> packetTitleConstructor;
    private static Object enumActionTitle;
    private static Object enumActionSubtitle;

    static {
        try {
            String version = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> craftPlayer = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
            Class<?> nmsPlayer = Class.forName("net.minecraft.server." + version + ".EntityPlayer");
            Class<?> packetClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutTitle");
            Class<?> actionClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutTitle$EnumTitleAction");

            getHandle = craftPlayer.getMethod("getHandle");
            enumActionTitle = actionClass.getEnumConstants()[0];
            enumActionSubtitle = actionClass.getEnumConstants()[1];

            Class<?> chatComponentText = Class.forName("net.minecraft.server." + version + ".ChatComponentText");
            Constructor<?> chatConst = chatComponentText.getConstructor(String.class);
            packetTitleConstructor = packetClass.getConstructor(actionClass, Class.forName("net.minecraft.server." + version + ".IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
            sendPacket = nmsPlayer.getField("playerConnection").getType().getMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet"));
        } catch (Exception e) {
            nmsDisponivel = false;
        }
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (!nmsDisponivel) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', title));
            if (subtitle != null && !subtitle.isEmpty()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', subtitle));
            }
            return;
        }
        try {
            String version = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> chatComponentText = Class.forName("net.minecraft.server." + version + ".ChatComponentText");
            Constructor<?> chatConst = chatComponentText.getConstructor(String.class);
            Object titleComp = chatConst.newInstance(ChatColor.translateAlternateColorCodes('&', title));
            Object subtitleComp = subtitle != null && !subtitle.isEmpty()
                    ? chatConst.newInstance(ChatColor.translateAlternateColorCodes('&', subtitle))
                    : chatConst.newInstance("");

            Object packetTitle = packetTitleConstructor.newInstance(enumActionTitle, titleComp, fadeIn, stay, fadeOut);
            Object packetSubtitle = packetTitleConstructor.newInstance(enumActionSubtitle, subtitleComp, fadeIn, stay, fadeOut);

            Object handle = getHandle.invoke(player);
            Object connection = handle.getClass().getField("playerConnection").get(handle);
            sendPacket.invoke(connection, packetTitle);
            sendPacket.invoke(connection, packetSubtitle);
        } catch (Exception e) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', title + " " + (subtitle != null ? subtitle : "")));
        }
    }

    public static void sendTitle(Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle, 10, 70, 20);
    }

    public static void sendCountdownTitle(Player player, int seconds) {
        sendTitle(player, "&e" + seconds, "&7Prepare-se!", 5, 20, 5);
    }
}
