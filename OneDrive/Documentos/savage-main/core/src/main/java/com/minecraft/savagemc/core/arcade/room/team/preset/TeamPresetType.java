package com.minecraft.savagemc.core.arcade.room.team.preset;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum TeamPresetType {

    RED("Vermelho", "V", ChatColor.RED, Color.RED),
    BLUE("Azul", "A", ChatColor.BLUE, Color.BLUE),
    PINK("Rosa", "R", ChatColor.LIGHT_PURPLE, Color.fromRGB(255, 85, 255)),
    ORANGE("Laranja", "L", ChatColor.GOLD, Color.ORANGE),
    YELLOW("Amarelo", "A", ChatColor.YELLOW, Color.YELLOW),
    GREEN("Verde", "V", ChatColor.GREEN, Color.GREEN),
    CYAN("Ciano", "C", ChatColor.AQUA, Color.AQUA),
    GRAY("Cinza", "C", ChatColor.DARK_GRAY, Color.GRAY);

    private final String name;
    private final String id;

    private final ChatColor color;
    private final Color rgb;

    public static TeamPresetType of(ChatColor color) {
        return Arrays.stream(values())
                .filter(team -> team.getColor().equals(color) || team.getRgb().toString().equalsIgnoreCase(color.name()))
                .findFirst()
                .orElse(null);
    }
}
