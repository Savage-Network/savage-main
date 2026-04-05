/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.util.game;

import com.minecraft.core.bukkit.util.reflection.ClassHandler;
import com.minecraft.savagemc.hungergames.HungerGames;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class GameStorage {
    @Getter
    private static List<Class<?>> games;

    static {
        games = new ArrayList<>(ClassHandler.getClassesForPackage(HungerGames.getInstance(), "com.minecraft.hungergames.game.list"));
        games.addAll(ClassHandler.getClassesForPackage(HungerGames.getInstance(), "com.minecraft.hungergames.game.list.events"));
    }

    public static Class<?> getGame(String name) {
        return games.stream().filter(c -> c.getSimpleName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
