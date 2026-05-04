package com.minecraft.arcade.eventos.eventos.spleef;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * Gera arena de Spleef: plataforma de neve que pode ser quebrada.
 */
public class SpleefMapGenerator {

    private static final int TAMANHO = 31;
    private static final int ALTURA_CAMADA = 2;
    private static final Material BLOCO = Material.SNOW_BLOCK;

    /** Gera a arena centrada em origin. Retorna o spawn (centro da plataforma, 1 bloco acima). */
    public static Location gerar(World world, Location origin) {
        int cx = origin.getBlockX();
        int cy = origin.getBlockY();
        int cz = origin.getBlockZ();
        int r = TAMANHO / 2;

        for (int camada = 0; camada < ALTURA_CAMADA; camada++) {
            int y = cy + camada;
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    if (x * x + z * z <= r * r) {
                        world.getBlockAt(cx + x, y, cz + z).setType(BLOCO);
                    }
                }
            }
        }

        return new Location(world, cx + 0.5, cy + ALTURA_CAMADA, cz + 0.5, 0, 0);
    }

    public static int getRaio() {
        return TAMANHO / 2;
    }

    public static boolean estaNaArena(Location loc, Location center) {
        if (loc.getWorld() != center.getWorld()) return false;
        int dx = loc.getBlockX() - center.getBlockX();
        int dz = loc.getBlockZ() - center.getBlockZ();
        return dx * dx + dz * dz <= getRaio() * getRaio();
    }
}
