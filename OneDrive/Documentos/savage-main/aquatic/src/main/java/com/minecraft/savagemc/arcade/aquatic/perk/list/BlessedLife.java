package com.minecraft.savagemc.arcade.aquatic.perk.list;

import com.minecraft.savagemc.arcade.aquatic.perk.Perk;
import com.minecraft.savagemc.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BlessedLife extends Perk {

    public BlessedLife() {
        super("Vida Extra", Item.of(Material.SKULL_ITEM, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTZjMDM0MjU1YTNkZDM4ZDVjZjk2M2ZlZDNlMzNmOGIzNTA0MGM1NzljZDI1NGI0ZmMzZDUzMmE3MjYwOGQxMiJ9fX0="));

        setLore("§7Receba §c5♥§7 corações extras!");
    }

    @Override
    public void handle(Player player) {
        player.setMaxHealth(30.0D);
        player.setHealth(30.0D);
    }
}
