package com.minecraft.savagemc.core.bukkit.api.cooldown;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class Cooldown {

    private final String name;

    private long duration, startTime = System.currentTimeMillis();

    private boolean showBar = true;

    public Cooldown(String name, long duration) {
        this.name = name;
        this.duration = duration;
    }

    public void update(long duration, long startTime) {
        this.duration = duration;
        this.startTime = startTime;
    }

    public double getRemaining() {
        long endTime = startTime + TimeUnit.SECONDS.toMillis(duration);
        return (-(System.currentTimeMillis() - endTime)) / 1000D;
    }

    public boolean isShowBar() {
        return showBar;
    }

    public long getEndTime() {
        return startTime + TimeUnit.SECONDS.toMillis(duration);
    }

    public boolean expired() {
        return getRemaining() < 0D;
    }

    public double getPercentage() {
        return (getRemaining() * 100) / duration;
    }

    // Métodos adicionais para compatibilidade
    public String getName() {
        return name;
    }

    public long getDuration() {
        return duration;
    }

    public long getStartTime() {
        return startTime;
    }
}
