package com.minecraft.savagemc.hungergames.user.object;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScrimSettings {

    private long cleanTime = 0L;
    private int hits = 0;

    public boolean isCleanTime() {
        return System.currentTimeMillis() < this.cleanTime;
    }

    public void resetClean() {
        this.cleanTime = 0L;
        this.hits = 0;
    }

    public void incrementHits() {
        this.hits++;
    }

}
