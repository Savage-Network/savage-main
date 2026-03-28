package com.minecraft.savagemc.core.backend.database;

public interface Database {

    void load();

    void unload();

    boolean isAvailable();
}
