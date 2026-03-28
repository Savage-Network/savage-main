package com.minecraft.savagemc.core.util.list.bukkit;

import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;
import java.util.List;

public class ColorUtil {

    public static int getIdByColor(ChatColor color) {
        List<ChatColor> colors = Arrays.asList(
                ChatColor.RED, ChatColor.BLUE, ChatColor.GREEN, ChatColor.YELLOW,
                ChatColor.LIGHT_PURPLE, ChatColor.GOLD, ChatColor.AQUA, ChatColor.DARK_GRAY
        );

        int[] ids = new int[]{14, 11, 5, 4, 2, 1, 3, 7};

        int indexOf = colors.indexOf(color);

        return ids[indexOf];
    }

    public static String getColorName(ChatColor color) {
        switch (color) {
            case RED:
                return "Vermelho";
            case BLUE:
                return "Azul";
            case GREEN:
                return "Verde";
            case YELLOW:
                return "Amarelo";
            case LIGHT_PURPLE:
                return "Rosa";
            case WHITE:
                return "Branco";
            case GOLD:
                return "Laranja";
            case DARK_PURPLE:
                return "Roxo";
            default:
                return "Desconhecida";
        }
    }
}
