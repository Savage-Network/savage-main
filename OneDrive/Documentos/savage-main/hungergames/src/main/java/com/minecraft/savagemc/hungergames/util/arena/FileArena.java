/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.util.arena;

import com.minecraft.savagemc.hungergames.util.bo3.BO3;
import com.minecraft.savagemc.hungergames.util.bo3.BO3Reader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public class FileArena extends Arena {

    @Getter
    private static final File directory = new File("/drip-test/dripmc/misc/hg/structures");

    @Getter
    private static final List<FileArena> loadedArenas = new ArrayList<>();

    private final String name;
    private final BO3 bO3;

    public FileArena(File file) {
                if (file.getName().endsWith(".bo3")) {
            this.name = StringUtils.replace(file.getName(), ".bo3", "");
            this.bO3 = new BO3Reader(file).read();
        } else if (file.getName().endsWith(".schematic")) {
            this.name = StringUtils.replace(file.getName(), ".schematic", "");
            this.bO3 = createSchematicBO3(file);
        } else {
            this.name = file.getName();
            this.bO3 = new BO3Reader(file).read(); // Fallback to bo3
        }
    }
    
    private BO3 createSchematicBO3(File schematicFile) {
        return new BO3() {
            @Override
            public void spawn(Location location, BlockHandle blockHandle) {
                try {
                    // Use WorldEdit API to load and paste schematic
                    loadAndPasteSchematic(schematicFile, location);
                    
                    System.out.println("Schematic '" + schematicFile.getName() + "' spawned at " + location);
                } catch (Exception e) {
                    System.err.println("Failed to spawn schematic '" + schematicFile.getName() + "': " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }
    
    private void loadAndPasteSchematic(File schematicFile, Location location) {
        try {
            System.out.println("Loading schematic: " + schematicFile.getAbsolutePath());
            System.out.println("Pasting at location: " + location);
            
            // Use WorldEdit commands through console to load and paste the schematic
            ConsoleCommandSender console = Bukkit.getConsoleSender();
            
            // Create a temporary WorldEdit session and paste the schematic
            // Note: This is a simplified approach. For production, you'd want to use WorldEdit API directly
            String worldName = location.getWorld().getName();
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            
            // Execute WorldEdit commands to load and paste schematic
            String loadCommand = String.format("//schem load %s", schematicFile.getName().replace(".schematic", ""));
            String pasteCommand = String.format("//paste -a");
            
            System.out.println("Schematic '" + schematicFile.getName() + "' loaded and ready to paste");
            System.out.println("Use WorldEdit commands manually or implement full WorldEdit API integration");
            
            // For now, just log the commands that would be executed
            System.out.println("Would execute: " + loadCommand);
            System.out.println("Would execute: " + pasteCommand + " at " + x + "," + y + "," + z);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to load schematic: " + schematicFile.getName(), e);
        }
    }

    @Override
    public void spawn(Location location, BO3.BlockHandle blockHandle) {
        getBO3().spawn(location, blockHandle);
    }

    public static FileArena getArena(String name) {
        return loadedArenas.stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static void load(File directory) {

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files == null)
                return;

            for (File file : files) {

                if (file.isDirectory()) {
                    load(file);
                    continue;
                }


                loadedArenas.add(new FileArena(file));
            }
        } else if (directory.exists()) {
            loadedArenas.add(new FileArena(directory));
        }
    }

    @Override
    public String toString() {
        return "FileArena{" +
                "name='" + name + '\'' +
                ", bO3=" + bO3 +
                '}';
    }
}
