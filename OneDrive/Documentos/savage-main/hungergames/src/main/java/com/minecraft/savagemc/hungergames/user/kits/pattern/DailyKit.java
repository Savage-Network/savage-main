package com.minecraft.savagemc.hungergames.user.kits.pattern;

import com.minecraft.core.Constants;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DailyKit {

    private String chosenKit = "...", movementKit = "...", strategyKit = "...", combatKit = "...";
    private long expiration = 0L;
    private int againRolls = 0;

    public String toJson() {
        return Constants.GSON.toJson(this);
    }

    public void incrementRolls() {
        againRolls++;
    }

    public boolean expired() {
        return expiration > System.currentTimeMillis();
    }

} 
