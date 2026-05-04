package com.minecraft.arcade.eventos.managers;

import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.schematic.EmbeddedSchematicPaste;
import com.minecraft.arcade.eventos.settings.EventEmbeddedConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Material;

import java.io.File;

/**
 * Pastas / spawns / bolo de fim de evento (schematic + bolos em código) — ver {@link #carregarAreaBolo()}.
 */
public class SchematicManager {

    private static final int[][] OFFSETS_CRUZ = buildOffsetsCruz();
    private static final int[][] OFFSETS_RETANGULO = new int[][]{{0, 0}, {1, 0}, {2, 0}, {0, 1}, {1, 1}, {2, 1}};
    private static final byte LIGHT_BLUE_GLASS_DATA = 3;

    private final Eventos plugin;
    private Location spawnEvento;
    private Location areaBoloLocation;

    public SchematicManager(Eventos plugin) {
        this.plugin = plugin;
        carregarLocations();
    }

    private void carregarLocations() {
        World w0 = Bukkit.getWorld(EventEmbeddedConfig.SPAWN_EVENTO_WORLD);
        if (w0 == null) {
            w0 = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        }
        spawnEvento = EventEmbeddedConfig.resolveSpawnEvento(w0);

        World wBolo = Bukkit.getWorld(EventEmbeddedConfig.AREA_BOLO_WORLD);
        if (wBolo == null && w0 != null) {
            wBolo = w0;
        }
        areaBoloLocation = EventEmbeddedConfig.resolveAreaBolo(wBolo != null ? wBolo : w0);
    }

    /** Atualiza spawn e área do bolo (útil após carregar mundos ou mudar de dimensão). */
    public void atualizarReferenciasDeMundo() {
        carregarLocations();
    }

    public void carregarMapaEvento() {
        String pasta = EventEmbeddedConfig.PASTA_SCHEMATICS;
        String nome = EventEmbeddedConfig.SCHEMATIC_EVENTO;
        File schematic = new File(new File(plugin.getDataFolder(), pasta), nome + ".schematic");
        if (schematic.exists()) colarSchematicSePossivel(schematic, spawnEvento);
    }

    /**
     * Monta a área de festa no fim do evento: tenta colar {@code area_bolo.schematic} (McEdit, motor embutido no plugin);
     * em seguida, se {@code event.bolo_auto} for true, coloca os blocos de bolo (formato cruz/retângulo em {@link EventEmbeddedConfig}).
     * Se não houver schematic e bolo_auto estiver desligado, gera só plataforma de pedra com um bolo central.
     */
    public void carregarAreaBolo() {
        atualizarReferenciasDeMundo();
        if (areaBoloLocation == null || areaBoloLocation.getWorld() == null) {
            plugin.getLogger().warning("[Eventos] Área do bolo: coordenadas ou mundo inválidos (ver EventEmbeddedConfig ou carregamento do mundo).");
            return;
        }

        boolean gerarBolos = plugin.getVariablesManager().getBoolean("event.bolo_auto", EventEmbeddedConfig.AREA_BOLO_GERAR_AUTOMATICO);

        String pasta = EventEmbeddedConfig.PASTA_SCHEMATICS;
        String nome = EventEmbeddedConfig.SCHEMATIC_BOLO;
        File dir = new File(plugin.getDataFolder(), pasta);
        File schematic = new File(dir, nome + ".schematic");
        boolean colado = schematic.exists() && colarSchematicEmbedded(schematic, areaBoloLocation);

        if (!colado) {
            if (gerarBolos) {
                gerarBoloAutomatico(true);
            } else {
                gerarPlataformaBoloSePreciso();
                colocarBoloCentralCelebracao();
            }
        } else if (gerarBolos) {
            // Com schematic colado: coloca os bolos por cima sem forçar camada de vidro por baixo (não estraga a build colada).
            gerarBoloAutomatico(false);
        }
    }

    @SuppressWarnings("deprecation")
    private void gerarBoloAutomatico(boolean aplicarBaseVidroEmbaixo) {
        if (areaBoloLocation == null || areaBoloLocation.getWorld() == null) return;
        String formato = EventEmbeddedConfig.AREA_BOLO_FORMATO;
        boolean baseVidro = EventEmbeddedConfig.AREA_BOLO_BASE_VIDRO;
        int[][] offsets = getOffsetsFormato(formato);
        World w = areaBoloLocation.getWorld();
        int cx = areaBoloLocation.getBlockX();
        int cy = areaBoloLocation.getBlockY();
        int cz = areaBoloLocation.getBlockZ();

        for (int[] offset : offsets) {
            int dx = cx + offset[0];
            int dz = cz + offset[1];
            if (baseVidro && aplicarBaseVidroEmbaixo) {
                org.bukkit.block.Block bloco = w.getBlockAt(dx, cy - 1, dz);
                bloco.setType(Material.STAINED_GLASS);
                bloco.setData(LIGHT_BLUE_GLASS_DATA);
            }
            w.getBlockAt(dx, cy, dz).setType(Material.CAKE_BLOCK);
        }
        plugin.getLogger().info("[Eventos] Bolos de celebração colocados (formato: " + formato + ", base vidro: " + (baseVidro && aplicarBaseVidroEmbaixo) + ").");
    }

    /** Um bolo no centro, um bloco acima da plataforma de pedra (fallback sem schematic e sem bolo_auto). */
    @SuppressWarnings("deprecation")
    private void colocarBoloCentralCelebracao() {
        if (areaBoloLocation == null || areaBoloLocation.getWorld() == null) return;
        World w = areaBoloLocation.getWorld();
        int cx = areaBoloLocation.getBlockX();
        int cy = areaBoloLocation.getBlockY();
        int cz = areaBoloLocation.getBlockZ();
        w.getBlockAt(cx, cy + 1, cz).setType(Material.CAKE_BLOCK);
        plugin.getLogger().info("[Eventos] Bolo central de celebração colocado (modo mínimo).");
    }

    private int[][] getOffsetsFormato(String formato) {
        if ("retangulo".equalsIgnoreCase(formato)) return OFFSETS_RETANGULO;
        return OFFSETS_CRUZ;
    }

    private static int[][] buildOffsetsCruz() {
        int[][] cruz = new int[21][2];
        int i = 0;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 && Math.abs(z) == 2) continue;
                cruz[i][0] = x;
                cruz[i][1] = z;
                i++;
            }
        }
        return cruz;
    }

    private void gerarPlataformaBoloSePreciso() {
        if (areaBoloLocation == null || areaBoloLocation.getWorld() == null) return;
        int raio = EventEmbeddedConfig.AREA_BOLO_RAIO_PLATAFORMA;
        int raioSq = raio * raio;
        World w = areaBoloLocation.getWorld();
        int cx = areaBoloLocation.getBlockX();
        int cy = areaBoloLocation.getBlockY();
        int cz = areaBoloLocation.getBlockZ();
        for (int x = -raio; x <= raio; x++) {
            for (int z = -raio; z <= raio; z++) {
                if (x * x + z * z <= raioSq) {
                    w.getBlockAt(cx + x, cy, cz + z).setType(Material.STONE);
                }
            }
        }
    }

    /** Colagem McEdit clássica embutida (NBT gzip), sem plugin WorldEdit. */
    private boolean colarSchematicEmbedded(File schematic, Location at) {
        if (at == null || at.getWorld() == null) return false;
        return EmbeddedSchematicPaste.paste(at, schematic, plugin.getLogger());
    }

    private void colarSchematicSePossivel(File schematic, Location at) {
        colarSchematicEmbedded(schematic, at);
    }

    public File getPastaSchematics() {
        return new File(plugin.getDataFolder(), EventEmbeddedConfig.PASTA_SCHEMATICS);
    }

    public Location getSpawnEvento() {
        return spawnEvento != null ? spawnEvento.clone() : null;
    }

    public Location getAreaBoloLocation() {
        return areaBoloLocation != null ? areaBoloLocation.clone() : null;
    }
}
