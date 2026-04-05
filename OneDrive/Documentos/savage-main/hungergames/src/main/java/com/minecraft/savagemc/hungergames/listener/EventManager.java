package com.minecraft.savagemc.hungergames.listener;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import lombok.Getter;

/**
 * Gerenciador de eventos do HungerGames
 * Registra e gerencia todos os listeners do plugin
 */
@Getter
public class EventManager {
    
    private final HungerGamesPlugin plugin;
    
    public EventManager(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Inicializa o gerenciador de eventos
     */
    public void initialize() {
        plugin.getLogger().info("Event Manager initialized");
        
        // Registrar listeners
        registerListeners();
        
        plugin.getLogger().info("Registered event listeners");
    }
    
    /**
     * Registra todos os listeners
     */
    private void registerListeners() {
        // Registrar listeners principais
        registerListener(new GameListener(plugin));
        registerListener(new PlayerListener(plugin));
        registerListener(new WorldListener(plugin));
        registerListener(new CombatListener(plugin));
        registerListener(new ConnectionListener(plugin));
    }
    
    /**
     * Registra um listener
     */
    public void registerListener(org.bukkit.event.Listener listener) {
        if (listener != null) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }
    
    /**
     * Desregistra um listener
     */
    public void unregisterListener(org.bukkit.event.Listener listener) {
        if (listener != null) {
            org.bukkit.event.HandlerList.unregisterAll(listener);
        }
    }
}
