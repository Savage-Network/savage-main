package com.minecraft.savagemc.hungergames.user.celebrations;

import com.minecraft.core.bukkit.util.reflection.ClassHandler;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.celebrations.list.Mlg;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Getter
public class CelebrationStorage {

    private final HungerGames hungerGames;
    private final List<Celebration> celebrations;

    public CelebrationStorage(HungerGames hungerGames) {
        this.hungerGames = hungerGames;
        this.celebrations = new ArrayList<>();
    }

    public CelebrationStorage enable() {
        getHungerGames().getLogger().info("Loading " + getClass().getSimpleName() + "...");

        celebrations.add(new Mlg(hungerGames));
        for (Class<?> classes : ClassHandler.getClassesForPackage(HungerGames.getInstance(), "com.minecraft.hungergames.user.celebrations.list")) {
            if (Celebration.class.isAssignableFrom(classes) && classes != Mlg.class) {
                try {
                    Celebration celebration = (Celebration) classes.getConstructor(HungerGames.class).newInstance(hungerGames);
                    hungerGames.getLogger().info("Loading the celebration " + celebration.getName() + "!");
                    celebrations.add(celebration);
                } catch (Exception e) {
                    e.printStackTrace();
                    hungerGames.getLogger().log(Level.SEVERE, "Celebration " + classes.getSimpleName() + " did not loaded properly due to an error. (" + e.getMessage() + ")");
                    Bukkit.shutdown();
                }
            }
        }

        getHungerGames().getLogger().info(getClass().getSimpleName() + " loaded successfully!");
        return this;
    }

    public void register() {
        for (Celebration celebration : getCelebrations())
            celebration.register();
    }

    public Celebration getCelebration(String name) {
        return getCelebrations().stream().filter(celebration -> celebration.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

}
