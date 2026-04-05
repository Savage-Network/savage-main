/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.util.game;

import com.minecraft.savagemc.hungergames.HungerGames;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

@Getter
public enum GameStage {

    WAITING, INVINCIBILITY, PLAYING, VICTORY, NONE;

    private final Collection<Listener> pendingRegister;
    private final Collection<Listener> pendingUnregister;

    GameStage() {
        this.pendingRegister = new HashSet<>();
        this.pendingUnregister = new HashSet<>();
    }

    public void register() {
        Iterator<Listener> iterator = getPendingRegister().iterator();
        while (iterator.hasNext()) {
            Listener listener = iterator.next();
            System.out.println(listener.getClass().getSimpleName() + " has been registered.");
            Bukkit.getPluginManager().registerEvents(listener, HungerGames.getInstance());
            iterator.remove();
        }
    }

    public void unregister() {
        Iterator<Listener> iterator = getPendingUnregister().iterator();
        while (iterator.hasNext()) {
            Listener listener = iterator.next();
            System.out.println(listener.getClass().getSimpleName() + " has been unregistered.");
            HandlerList.unregisterAll(listener);
            iterator.remove();
        }
    }
}
