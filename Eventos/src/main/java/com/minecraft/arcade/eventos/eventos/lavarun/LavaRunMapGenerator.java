package com.minecraft.arcade.eventos.eventos.lavarun;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * Gera arena Lava Run: pista de blocos com lava embaixo. Blocos somem ao pisar.
 */
public class LavaRunMapGenerator {

    private static final int LARGURA = 5;
    private static final int COMPRIMENTO = 41;
    private static final Material BLOCO = Material.SANDSTONE;
    private static final Material LAVA = Material.LAVA;

    public static Location gerar(World world, Location origin) {
        int cx = origin.getBlockX();
        int cy = origin.getBlockY();
        int cz = origin.getBlockZ();

        for (int z = 0; z < COMPRIMENTO; z++) {
            for (int x = -LARGURA / 2; x <= LARGURA / 2; x++) {
                world.getBlockAt(cx + x, cy, cz + z).setType(LAVA);
                world.getBlockAt(cx + x, cy + 1, cz + z).setType(BLOCO);
            }
        }

        return new Location(world, cx + 0.5, cy + 2, cz + 0.5, 0, 0);
    }

    public static boolean estaNaPista(Location loc, Location center) {
        if (loc.getWorld() != center.getWorld()) return false;
        int dx = loc.getBlockX() - center.getBlockX();
        int dz = loc.getBlockZ() - center.getBlockZ();
        return Math.abs(dx) <= LARGURA / 2 && dz >= 0 && dz < COMPRIMENTO;
    }

    public static int getComprimento() {
        return COMPRIMENTO;
    }
}
