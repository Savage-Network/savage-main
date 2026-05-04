package com.minecraft.arcade.eventos.eventos.sumo;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * Gera arena de Sumo: plataforma circular pequena. Quem cair ou sair perde.
 */
public class SumoMapGenerator {

    private static final int RAIO = 8;
    private static final int ALTURA = 2;
    private static final Material BLOCO = Material.WOOL;

    public static Location gerar(World world, Location origin) {
        int cx = origin.getBlockX();
        int cy = origin.getBlockY();
        int cz = origin.getBlockZ();

        for (int camada = 0; camada < ALTURA; camada++) {
            int y = cy + camada;
            for (int x = -RAIO; x <= RAIO; x++) {
                for (int z = -RAIO; z <= RAIO; z++) {
                    if (x * x + z * z <= RAIO * RAIO) {
                        world.getBlockAt(cx + x, y, cz + z).setType(BLOCO);
                    }
                }
            }
        }

        return new Location(world, cx + 0.5, cy + ALTURA, cz + 0.5, 0, 0);
    }

    public static int getRaio() {
        return RAIO;
    }

    public static double getYBase(Location center) {
        return center.getBlockY() + ALTURA;
    }

    public static boolean estaNaArena(Location loc, Location center) {
        if (loc.getWorld() != center.getWorld()) return false;
        int dx = loc.getBlockX() - center.getBlockX();
        int dz = loc.getBlockZ() - center.getBlockZ();
        double yBase = center.getBlockY() + ALTURA;
        return dx * dx + dz * dz <= RAIO * RAIO && loc.getY() >= yBase - 1;
    }
}
