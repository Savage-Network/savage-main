package com.minecraft.savagemc.core.bungee.bot.command.structure.manager;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.bungee.BungeeCore;
import com.minecraft.savagemc.core.bungee.bot.command.structure.SlashCommand;
import com.minecraft.savagemc.core.util.list.loader.ClassLoader;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class SlashCommandManager {

    @Getter
    private static final Set<SlashCommand> commands = new HashSet<>();

    public static void handle(String commandDir) {
        int loaded = 0;

        Savage.Savage.Logger().info("[Slash] Registrando comandos...");

        for (Class<?> commandClass : ClassLoader.getClassesForPackage(BungeeCore.getInstance(), commandDir)) {
            if (SlashCommand.class.isAssignableFrom(commandClass)) {
                try {
                    SlashCommand command = (SlashCommand) commandClass.newInstance();

                    command.builder();

                    commands.add(command);
                    loaded++;
                } catch (Exception e) {
                    Savage.Savage.Logger().log(Level.WARNING, "Não foi possível registrar o slash command " + commandClass.getSimpleName(), e);
                }
            }
        }

        if (loaded >0 )
            Savage.Savage.Logger().info("[Slash] Registro de comandos concluído. (" + loaded + " comandos registrados)");
    }

    public static SlashCommand of(String name) {
        return commands.stream().filter(slash -> slash.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
