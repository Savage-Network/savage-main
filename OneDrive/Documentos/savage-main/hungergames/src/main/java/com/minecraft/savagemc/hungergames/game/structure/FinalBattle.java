/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.game.structure;

import com.minecraft.core.bukkit.event.player.PlayerMassiveTeleportExecuteEvent;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.util.arena.FileArena;
import com.minecraft.savagemc.hungergames.util.arena.shape.Square;
import com.minecraft.savagemc.hungergames.util.bo3.BO3;
import com.minecraft.savagemc.hungergames.util.bo3.EmptyBO3;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import com.minecraft.savagemc.hungergames.game.Game;
import com.minecraft.savagemc.hungergames.util.metadata.GameMetadata;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class FinalBattle implements Assistance {

    private final Location location;
    private boolean disableKits;
    private Game game;
    private BO3 bo3;

    public FinalBattle(Location location) {
        this.location = location;
    }

    public void prepare() {
        if (game.getName() == "legion") {

            FileArena fileArena = FileArena.getArena("arena_medium");

            if (fileArena == null) {
                File dir = new File("Savage/hg/structures/arena_medium");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileArena.load(dir);
                fileArena = FileArena.getArena("arena_medium");
            }

            this.bo3 = fileArena.getBO3();

            List<Block> blocks = new ArrayList<>();

            bo3.spawn(location, (loc, pattern) -> {
                Block block = loc.getBlock();

                if (pattern.getMaterial() == Material.LAPIS_BLOCK) {
                    block.setType(Material.AIR);

                    for (int i = 0; i < 100; i++) {
                        block = block.getRelative(BlockFace.UP);

                        if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                            WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.AIR, (byte) 0);
                        }
                    }
                    return false;
                } else if (pattern.getMaterial() == Material.GRASS && block.getBiome() == Biome.DESERT) {
                    pattern.setMaterial(Material.SAND);
                }

                if (pattern.getMaterial() != Material.AIR) {
                    blocks.add(block);
                    block.setMetadata("unbreakable", new GameMetadata(0));
                }

                return true;
            });

            this.bo3 = new EmptyBO3();

        } else {

            List<Location> blocks = WorldEditAPI.getInstance().makeCylinder(location, 22, 22, 1, true);

            blocks.stream().map(Location::getBlock).forEach(block -> {
                WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.AIR, (byte) 0);

                while (block.getY() < getGame().getVariables().getWorldHeight()) {
                    if (block.getType() == Material.AIR) {
                        block = block.getRelative(BlockFace.UP);
                        continue;
                    }

                    Location blockLocation = block.getLocation();
                    WorldEditAPI.getInstance().setBlock(blockLocation, Material.AIR, (byte) 0);
                    block = block.getRelative(BlockFace.UP);
                }
            });

            Square square = new Square(
                    35,
                    (getGame().getVariables().getWorldHeight() - location.getBlockY()),
                    Collections.singletonList(Pattern.of(Material.BEDROCK)),
                    false,
                    false
            );

            square.spawn(location, (loc, pattern) -> {
                if (loc.getBlock().getType() != Material.BEDROCK)
                    WorldEditAPI.getInstance().setBlock(loc, Material.BEDROCK, (byte) 0, false);
                return false;
            });
        }
    }


    public void teleport() {

        if (disableKits) {
            for (Kit kit : getPlugin().getKitStorage().getKits()) {
                if (kit.isActive())
                    kit.setActive(false, true);
            }
        }

        Location loc = this.location.clone().add(0.5, 0.8, 0.5);

        if (game.getName() == "legion") {
            getGame().getVariables().setWorldHeight(80);
            getGame().getVariables().setWorldSize(50);

            try {
                getPlugin().getVariableLoader().getVariable("hg.border.damage").setValue(6);
            } catch (Exception e) {
                System.out.println("The plugin can't change border damage for champion.");
            }

        } else {
            getGame().getVariables().setWorldHeight(50);
            getGame().getVariables().setWorldSize(38);

            try {
                getPlugin().getVariableLoader().getVariable("hg.border.damage").setValue(4);
            } catch (Exception e) {
                System.out.println("The plugin can't change border damage.");
            }
        }

        PlayerMassiveTeleportExecuteEvent event = new PlayerMassiveTeleportExecuteEvent(
                getPlugin().getUserStorage().getUsers().stream().map(User::getPlayer).collect(Collectors.toSet()),
                loc
        );
        event.fire();

        event.getRecipients().forEach(c -> {
            Player player = c.getPlayer();
            player.setNoDamageTicks(20);
            player.setFallDistance(-1);
            player.teleport(loc);
        });
    }


    public void setDisableKits(boolean disableKits) {
        this.disableKits = disableKits;
    }
}
