package com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.event.phase;

import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.event.action.EventAction;
import com.minecraft.savagemc.arcade.bedwars.structure.team.Team;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.BedOre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
public enum EventPhase {

    /* Eventos de Geradores */
    DIAMOND_GENERATOR_II("Diamante II", arena -> {
        arena.upgradeGenerators(BedOre.DIAMOND, 25);

        arena.send("§bGerador de Diamante§e evoluiu para o nível §cII§e.");
    }),
    EMERALD_GENERATOR_II("Esmeralda II", arena -> {
        arena.upgradeGenerators(BedOre.EMERALD, 40);

        arena.send("§aGerador de Esmeralda§e evoluiu para o nível §cII§e.");
    }),
    DIAMOND_GENERATOR_III("Diamante III", arena -> {
        arena.upgradeGenerators(BedOre.DIAMOND, 15);

        arena.send("§bGerador de Diamante§e evoluiu para o nível §cIII§e.");
    }),
    EMERALD_GENERATOR_III("Esmeralda III", arena -> {
        arena.upgradeGenerators(BedOre.EMERALD, 25);

        arena.send("§aGerador de Esmeralda§e evoluiu para o nível §cIII§e.");
    }),
    SUDDEN_DEATH("Morte Súbita", arena -> {
        arena.getContext().setEvent(null);

        for (Team team : arena.getTeamList())
            Savage.getPlatform().runSync(team::destructBed);

        arena.getUsers().forEach(user -> CompletableFuture.runAsync(() -> arena.getArcade().handleSidebar(user)));

        arena.send("§5§lMORTE SÚBITA: §cTodas as camas foram removidas.");
        arena.sound(Sound.ENDERDRAGON_GROWL, 13, 1);
    });

    private final String name;
    private final EventAction action;
}
