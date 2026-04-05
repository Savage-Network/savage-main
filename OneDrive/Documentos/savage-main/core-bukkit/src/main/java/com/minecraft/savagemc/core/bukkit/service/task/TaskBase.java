package com.minecraft.savagemc.core.bukkit.service.task;

import org.bukkit.plugin.Plugin;

public abstract class TaskBase implements Runnable {

    protected Plugin plugin;

    public TaskBase(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public abstract void init();
}
