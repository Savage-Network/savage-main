package com.minecraft.savagemc.core.bukkit.api.firework;

import com.minecraft.savagemc.core.Savage;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Arrays;

public class FireworkApi {

    public static void random(Location location) {
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);

        FireworkMeta fireworkMeta = firework.getFireworkMeta();

        int id = Savage.RANDOM.nextInt(4) + 1;

        FireworkEffect effect = FireworkEffect.builder()
                .flicker(Savage.RANDOM.nextBoolean())
                .withColor(Color.WHITE)
                .withColor(Color.YELLOW)
                .withFade(Color.AQUA)
                .with(findType(id))
                .trail(Savage.RANDOM.nextBoolean())
                .build();

        fireworkMeta.addEffect(effect);
        fireworkMeta.setPower(Savage.RANDOM.nextInt(2) + 1);

        firework.setFireworkMeta(fireworkMeta);
    }

    protected static FireworkEffect.Type findType(int id) {
        return Arrays.stream(FireworkEffect.Type.values()).filter(effect -> effect.ordinal() == id).findFirst().orElse(null);
    }
}
