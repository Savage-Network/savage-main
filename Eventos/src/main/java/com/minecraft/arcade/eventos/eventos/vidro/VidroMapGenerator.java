package com.minecraft.arcade.eventos.eventos.vidro;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * Arena circular de vidro colorido — quebre e não caia (estilo Spleef).
 */
public final class VidroMapGenerator {

    private static final int TAMANHO = 31;
    private static final int ALTURA_CAMADA = 2;
    /** Dye data: 11 = azul no 1.8 */
    private static final byte COR_VIDRO = 11;

    private VidroMapGenerator() {
    }

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
                        org.bukkit.Block b = world.getBlockAt(cx + x, y, cz + z);
                        b.setType(Material.STAINED_GLASS);
                        b.setData(COR_VIDRO);
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

    public static boolean eVidroDaArena(Material type) {
        return type == Material.STAINED_GLASS || type == Material.THIN_GLASS || type == Material.GLASS;
    }
}
