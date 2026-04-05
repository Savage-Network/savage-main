package com.minecraft.savagemc.arcade.aquatic.controller;

import com.minecraft.savagemc.arcade.aquatic.Aquatic;
import com.minecraft.savagemc.arcade.aquatic.perk.Perk;
import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.util.list.loader.ClassLoader;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PerkController {

    @Getter
    private final static Set<Perk> perkList = new HashSet<>();

    static {
        for (Class<?> perkClass : ClassLoader.getClassesForPackage(Aquatic.getInstance(), Constant.SOURCE_DIR + ".arcade.aquatic.perk.list")) {
            if (Perk.class.isAssignableFrom(perkClass)) {
                try {
                    Perk perk = (Perk) perkClass.newInstance();

                    perkList.add(perk);
                } catch (Exception e) {
                    Savage.getLogger().log(Level.SEVERE, "Não foi possível registrar a perk " + perkClass.getSimpleName(), e);
                }
            }
        }
    }

    public static List<Perk> getAvailablePerks(Account account) {
        return perkList.stream()
                .filter(perk -> perk.has(account))
                .collect(Collectors.toList());
    }

    public static Perk of(String name) {
        return perkList.stream().filter(perk -> perk.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static Perk of(Class<? extends Perk> perkClass) {
        return perkList.stream().filter(perk -> perkClass.isAssignableFrom(perk.getClass())).findFirst().orElse(null);
    }
}
