package com.minecraft.savagemc.core.bukkit.api.menu.sound;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Sound;

@Getter
public enum MenuSound {

    DONE(Sound.ITEM_PICKUP),
    ERROR(Sound.NOTE_BASS),
    SUCCESS(Sound.ORB_PICKUP),
    PAGINATED(Sound.CLICK);

    private final Sound sound;

    MenuSound(Sound sound) {
        this.sound = sound;
    }

    public Sound getSound() {
        return sound;
    }
}
