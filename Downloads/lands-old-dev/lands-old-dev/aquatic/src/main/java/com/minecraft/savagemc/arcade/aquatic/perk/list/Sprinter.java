package com.minecraft.savagemc.arcade.aquatic.perk.list;

import com.minecraft.savagemc.arcade.aquatic.perk.Perk;
import com.minecraft.savagemc.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Sprinter extends Perk {

    public Sprinter() {
        super("Velocista", Item.of(Material.COOKIE));

        setLore("§7Torne-se um homem super veloz.");
    }

    @Override
    public void handle(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
    }
}
