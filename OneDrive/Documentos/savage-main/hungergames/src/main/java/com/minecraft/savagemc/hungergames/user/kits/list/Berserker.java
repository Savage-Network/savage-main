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
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class  Berserker extends Kit {

    public Berserker(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.SKULL_ITEM, 2));
        setKitCategory(KitCategory.COMBAT);
    }

    @Override
    public void appreciate(LivingUserDieEvent event) {

        Player player = event.getKiller().getPlayer();

        int time = 240;

        if (player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            PotionEffect potionEffect = player.getActivePotionEffects().stream().filter(c -> c.getType() == PotionEffectType.INCREASE_DAMAGE).findAny().orElse(null);
            if (potionEffect != null)
                time += potionEffect.getDuration();
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, time, 0), true);
    }
}
