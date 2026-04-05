package com.minecraft.savagemc.core.bukkit.manager;

import com.minecraft.savagemc.core.bukkit.manager.list.*;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.Getter;

@Getter
public class Manager {

    private final ProtocolManager protocol = ProtocolLibrary.getProtocolManager();

    private final SidebarManager sidebar = new SidebarManager();
    private final MenuManager menu = new MenuManager();

    private final ArcadeManager arcade = new ArcadeManager();
    private final ArcadeConfigManager arcadeConfig = new ArcadeConfigManager();

    private final NpcManager npc = new NpcManager();
    private final HologramManager hologram = new HologramManager();

    private final CooldownManager cooldown = new CooldownManager();

    // Métodos adicionais para compatibilidade
    public NpcManager getNpc() {
        return npc;
    }

    public ProtocolManager getProtocol() {
        return protocol;
    }

    public SidebarManager getSidebar() {
        return sidebar;
    }

    public ArcadeManager getArcade() {
        return arcade;
    }

    public CooldownManager getCooldown() {
        return cooldown;
    }

    public HologramManager getHologram() {
        return hologram;
    }

    public MenuManager getMenu() {
        return menu;
    }

    public ArcadeConfigManager getArcadeConfig() {
        return arcadeConfig;
    }
}
