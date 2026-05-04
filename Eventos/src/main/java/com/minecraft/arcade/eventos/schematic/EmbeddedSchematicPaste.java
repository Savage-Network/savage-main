package com.minecraft.arcade.eventos.schematic;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Colagem de {@code .schematic} formato McEdit (NBT gzip) usando apenas Bukkit API — sem WorldEdit/JAR externo.
 */
public final class EmbeddedSchematicPaste {

    private EmbeddedSchematicPaste() {
    }

    /**
     * Cola o schematic na origem (primeiro canto inferior mínimo: bloco mais a oeste-baixo-norte típico do McEdit).
     *
     * @return {@code false} se ficheiro inválido, mundo nulo ou exceção
     */
    @SuppressWarnings("deprecation")
    public static boolean paste(Location origin, File file, Logger logger) {
        if (origin == null || origin.getWorld() == null || file == null || !file.isFile()) {
            return false;
        }
        World world = origin.getWorld();
        try {
            MceditSchematicLoader sch = MceditSchematicLoader.fromFile(file);
            int w = sch.getWidth() & 0xFFFF;
            int h = sch.getHeight() & 0xFFFF;
            int l = sch.getLength() & 0xFFFF;
            int ox = origin.getBlockX();
            int oy = origin.getBlockY();
            int oz = origin.getBlockZ();

            int placed = 0;
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < l; z++) {
                    for (int x = 0; x < w; x++) {
                        int id = sch.blockAt(x, y, z);
                        if (id < 0) continue;

                        Material mat = id == 0 ? Material.AIR : Material.getMaterial(id);
                        if (mat == null) {
                            mat = Material.AIR;
                        }

                        Block b = world.getBlockAt(ox + x, oy + y, oz + z);
                        b.setType(mat);
                        if (mat != Material.AIR) {
                            b.setData((byte) sch.metaAt(x, y, z));
                            placed++;
                        }
                    }
                }
            }

            if (logger != null) {
                logger.info("[Eventos] Schematic embutido colado (" + placed + " blocos aplicados): " + file.getName()
                        + " (" + w + "×" + h + "×" + l + ")");
            }
            return true;
        } catch (Exception e) {
            if (logger != null) {
                logger.log(Level.WARNING, "[Eventos] Falha ao colar schematic embutido: " + file.getName(), e);
            }
            return false;
        }
    }
}
