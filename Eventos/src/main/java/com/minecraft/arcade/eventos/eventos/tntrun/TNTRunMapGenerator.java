package com.minecraft.arcade.eventos.eventos.tntrun;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * Gera arena TNT Run: várias camadas de blocos que somem ao pisar.
 */
public class TNTRunMapGenerator {

    private static final int TAMANHO = 25;
    private static final int CAMADAS = 3;
    private static final Material BLOCO = Material.SANDSTONE;

    public static Location gerar(World world, Location origin) {
        int cx = origin.getBlockX();
        int cy = origin.getBlockY();
        int cz = origin.getBlockZ();
        int r = TAMANHO / 2;

        for (int camada = 0; camada < CAMADAS; camada++) {
            int y = cy + camada;
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    if (x * x + z * z <= r * r) {
                        world.getBlockAt(cx + x, y, cz + z).setType(BLOCO);
                    }
                }
            }
        }

        return new Location(world, cx + 0.5, cy + CAMADAS, cz + 0.5, 0, 0);
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
