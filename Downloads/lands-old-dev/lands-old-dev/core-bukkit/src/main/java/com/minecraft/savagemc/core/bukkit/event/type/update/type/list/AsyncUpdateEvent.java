package com.minecraft.savagemc.core.bukkit.event.type.update.type.list;

import com.minecraft.savagemc.core.bukkit.event.type.update.UpdateEvent;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.UpdateType;

public class AsyncUpdateEvent extends UpdateEvent {
    public AsyncUpdateEvent(UpdateType type, long ticks) {
        super(type, ticks);
    }
}
