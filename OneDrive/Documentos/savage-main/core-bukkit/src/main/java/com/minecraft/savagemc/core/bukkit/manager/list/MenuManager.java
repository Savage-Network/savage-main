package com.minecraft.savagemc.core.bukkit.manager.list;

import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.controller.Controller;

import java.util.UUID;

public class MenuManager extends Controller<UUID, Menu> {

    @Override
    public void save(Menu menu) {
        getCache().put(menu.getPlayer().getUniqueId(), menu);
    }
}
