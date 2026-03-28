package com.minecraft.savagemc.core.bukkit.manager.list;

import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.controller.Controller;
import org.bukkit.entity.Player;

public class SidebarManager extends Controller<Player, Sidebar> {

    @Override
    public void save(Sidebar sidebar) {
        getCache().put(sidebar.getOwner(), sidebar);
    }
}
