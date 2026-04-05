package com.minecraft.savagemc.core.bukkit.event.type.update;

import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.UpdateType;
import lombok.Getter;

@Getter
public class UpdateEvent extends EventHandler {

    protected final UpdateType type;
    protected final long ticks;

    public UpdateEvent(UpdateType type, long ticks) {
        this.type = type;
        this.ticks = ticks;
    }

    public boolean isType(UpdateType type) {
        return this.type.equals(type);
    }

    public int getSecond() {
        return (int) ticks / 20;
    }
}
