package com.minecraft.savagemc.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class Urgal extends Kit {

    public Urgal(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.POTION, 8265));
        setKitCategory(KitCategory.STRATEGY);
        setPrice(25000);

        Potion potion = new Potion(PotionType.STRENGTH);

        ItemStack itemStack = potion.toItemStack(3);
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();

        potionMeta.clearCustomEffects();
        potionMeta.setDisplayName("§aPoção de força");
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 0), true);

        itemStack.setItemMeta(potionMeta);

        setItems(itemStack);
    }

}
