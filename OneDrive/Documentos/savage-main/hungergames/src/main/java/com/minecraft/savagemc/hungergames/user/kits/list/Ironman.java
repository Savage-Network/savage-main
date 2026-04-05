/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Ironman extends Kit {

    public Ironman(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.IRON_INGOT));
        setKitCategory(KitCategory.STRATEGY);
        setPrice(25000);
    }

    @Override
    public void appreciate(LivingUserDieEvent event) {
        giveItem(event.getKiller().getPlayer(), new ItemStack(Material.IRON_INGOT, 1), event.getKiller().getPlayer().getLocation().clone().add(0, 0.5, 0));
    }

}
