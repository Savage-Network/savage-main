package com.minecraft.savagemc.hungergames.util.explication;

import com.minecraft.savagemc.hungergames.HungerGames;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class Explication {

    private final int interval;
    private int current;
    @Setter
    private boolean done;
    @Setter
    private boolean started;
    private final String[] messages;

    public Explication(int interval, String... strings) {
        this.messages = strings;
        this.interval = interval;
        this.current = 0;
        this.done = false;
    }

    public int getNecessaryTime() {
        return interval * (messages.length - 1);
    }

    public String next() {
        if (this.current == messages.length) {
            this.done = true;
            this.current = 0;
        }
        String message = messages[this.current];
        this.current++;
        return message;
    }

    public String previous() {
        current--;
        return messages[this.current];
    }

    public void run() {
        new BukkitRunnable() {
            @Override
            public void run() {

                String message = next();

                if (isDone()) {
                    cancel();
                    return;
                }

                Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage("§b§lLEGION §7» §r" + message));
            }
        }.runTaskTimer(HungerGames.getEngine(), 0, (getInterval() * 20L));
    }
}
