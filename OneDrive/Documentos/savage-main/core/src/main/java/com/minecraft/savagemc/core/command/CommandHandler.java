package com.minecraft.savagemc.core.command;

public interface CommandHandler {

    void handle(String path);

    void registerInheritor(CommandInheritor inheritor);
}
