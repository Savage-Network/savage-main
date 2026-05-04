package com.minecraft.arcade.eventos.eventos.folha;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * Gera arena de Folha: uma camada de folhas que podem ser quebradas, com borda na beirada.
 */
public class FolhaMapGenerator {

    private static final int TAMANHO = 51;
    private static final int ALTURA_CAMADA = 1;
    private static final Material BLOCO = Material.LEAVES;
    private static final Material BORDA = Material.WOOD;
    private static final int DATA_LEAVES = 0;

    public static Location gerar(World world, Location origin) {
        int cx = origin.getBlockX();
        int cy = origin.getBlockY();
        int cz = origin.getBlockZ();
        int r = TAMANHO / 2;

        for (int camada = 0; camada < ALTURA_CAMADA; camada++) {
            int y = cy + camada;
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    int dx = x;
                    int dz = z;
                    int distSq = dx * dx + dz * dz;
                    if (distSq <= r * r) {
                        if (distSq >= (r - 1) * (r - 1)) {
                            world.getBlockAt(cx + x, y, cz + z).setType(BORDA);
                        } else {
                            world.getBlockAt(cx + x, y, cz + z).setType(BLOCO);
                            try {
                                world.getBlockAt(cx + x, y, cz + z).setData((byte) DATA_LEAVES);
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }

        return new Location(world, cx + 0.5, cy + ALTURA_CAMADA, cz + 0.5, 0, 0);
    }

    public static int getRaio() {
        return TAMANHO / 2;
    }

    /** Raio da área jogável (só folhas, sem a borda). */
    public static int getRaioJogavel() {
        return TAMANHO / 2 - 1;
    }

    /** Verifica se a localização está dentro da arena (incluindo borda). */
    public static boolean estaNaArena(Location loc, Location center) {
        if (loc.getWorld() != center.getWorld()) return false;
        int dx = loc.getBlockX() - center.getBlockX();
        int dz = loc.getBlockZ() - center.getBlockZ();
        return dx * dx + dz * dz <= getRaio() * getRaio();
    }
}
