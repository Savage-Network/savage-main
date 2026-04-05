/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.util.templatekit;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;

@Getter
@Setter
public class TemplateKit {

    private final String name;
    private ItemStack[] contents, armorContents;
    private Collection<PotionEffect> effects;
    private int experience;

    public TemplateKit(String name) {
        this.name = name;
        this.experience = -1;
        this.effects = null;
    }

    @Override
    public String toString() {
        return "TemplateKit{" +
                "name='" + name + '\'' +
                ", expierence=" + experience +
                '}';
    }
}
