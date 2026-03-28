package com.minecraft.savagemc.core.bungee.listener.handler;

import com.minecraft.savagemc.core.bungee.BungeeCore;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.event.IgnoreEvent;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.core.util.list.loader.ClassLoader;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ListenerHandler {

    private final BungeeCore bungee;

    public void handle(String path) {
        Instant now = Instant.now();

        int loaded = 0;

        Savage.Savage.Logger().info("Registrando eventos...");

        for (Class<?> listenerClass : ClassLoader.getClassesForPackage(bungee, path)) {
            if (Listener.class.isAssignableFrom(listenerClass)) {
                if (listenerClass.isAnnotationPresent(IgnoreEvent.class)) continue;

                try {
                    Listener listener = (Listener) listenerClass.newInstance();

                    bungee.getProxy().getPluginManager().registerListener(bungee, listener);

                    for (Method method : listenerClass.getMethods()) {
                        if (method.isAnnotationPresent(EventHandler.class))
                            loaded++;
                    }

                } catch (Exception e) {
                    Savage.Savage.Logger().log(Level.SEVERE, "Não foi possível registrar o evento " + listenerClass.getSimpleName(), e);
                }
            }
        }

        if (loaded > 0)
            Savage.Savage.Logger().info("Registro de eventos concluído. (Total de eventos registrados: " + loaded + " em " + Util.formatInstant(now) + ")");
    }
}
