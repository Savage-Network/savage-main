package com.minecraft.savagemc.hungergames.config;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import lombok.Getter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.logging.Level;

/**
 * Gerenciador de configurações do HungerGames
 * Responsável por gerenciar configs, arenas e recursos
 */
@Getter
public class ConfigurationManager {

    private final HungerGamesPlugin plugin;
    private File dataFolder;
    private File arenasFolder;
    private File structuresFolder;
    private File mapsFolder;

    public ConfigurationManager(HungerGamesPlugin plugin) {
        this.plugin = plugin;
        createDirectories();
    }

    /**
     * Inicializa o gerenciador de configurações
     */
    public void initialize() {
        plugin.getLogger().info("Configuration Manager initialized");
        
        // Carregar configurações padrão
        loadDefaultConfigurations();
        
        // Validar estrutura de diretórios
        validateDirectoryStructure();
    }

    /**
     * Cria a estrutura de diretórios necessária
     */
    private void createDirectories() {
        dataFolder = plugin.getDataFolder();
        dataFolder.mkdirs();

        arenasFolder = new File(dataFolder, "arenas");
        arenasFolder.mkdirs();

        structuresFolder = new File(dataFolder, "structures");
        structuresFolder.mkdirs();

        mapsFolder = new File(dataFolder, "maps");
        mapsFolder.mkdirs();
    }

    /**
     * Carrega configurações padrão
     */
    private void loadDefaultConfigurations() {
        // Salvar config.yml padrão se não existir
        plugin.saveDefaultConfig();
        
        // Recarregar configurações
        plugin.reloadConfig();
    }

    /**
     * Valida a estrutura de diretórios
     */
    private void validateDirectoryStructure() {
        if (!dataFolder.exists()) {
            plugin.getLogger().warning("Data folder not found: " + dataFolder.getAbsolutePath());
        }
        
        if (!arenasFolder.exists()) {
            plugin.getLogger().info("Creating arenas folder: " + arenasFolder.getAbsolutePath());
            arenasFolder.mkdirs();
        }
        
        if (!structuresFolder.exists()) {
            plugin.getLogger().info("Creating structures folder: " + structuresFolder.getAbsolutePath());
            structuresFolder.mkdirs();
        }
        
        if (!mapsFolder.exists()) {
            plugin.getLogger().info("Creating maps folder: " + mapsFolder.getAbsolutePath());
            mapsFolder.mkdirs();
        }
    }

    /**
     * Extrai estruturas padrão do resources
     */
    public void extractDefaultStructures() {
        String[] structures = {
            "spawn/spawn.bo3", 
            "spawn/spawn2.bo3", 
            "feast/feast.bo3", 
            "minifeast/minifeast.bo3", 
            "border/border.schematic"
        };
        
        for (String structure : structures) {
            extractResource("structures/" + structure, new File(structuresFolder, structure));
        }
    }

    /**
     * Extrai mapas padrão do resources
     */
    public void extractDefaultMap() {
        // Verificar se já existem mapas
        File[] existingMaps = mapsFolder.listFiles(File::isDirectory);
        if (existingMaps != null && existingMaps.length > 0) {
            plugin.getLogger().info("Maps directory already contains " + existingMaps.length + " map(s)");
            return;
        }

        plugin.getLogger().info("No maps found, extracting bundled maps...");

        String[][] bundledMaps = {
            {"map-3dce470b-5b98-4c06-ad3d-6df7118c0143", "r.-1.-1.mca", "r.-1.0.mca", "r.0.-1.mca", "r.0.0.mca"},
            {"map-5c4c772c-e2e6-4b16-b1d2-743fec801015", "r.-1.-1.mca", "r.-1.0.mca", "r.-1.1.mca", "r.-2.0.mca", "r.0.-1.mca", "r.0.0.mca", "r.0.1.mca", "r.0.2.mca"},
            {"map-89d532f0-b5f3-472e-8ae0-2bee9626b09d", "r.-1.-1.mca", "r.-1.0.mca", "r.0.-1.mca", "r.0.-2.mca", "r.0.0.mca", "r.1.-1.mca", "r.1.0.mca"},
            {"map-8f653b7d-b766-4bba-81c7-51c9ae14c2de", "r.-1.-1.mca", "r.-1.0.mca", "r.0.-1.mca", "r.0.0.mca"},
            {"map-b33deced-2f9d-40bd-a679-087b1896d744", "r.-1.-1.mca", "r.-1.0.mca", "r.-2.-1.mca", "r.-2.0.mca", "r.0.-1.mca", "r.0.0.mca"},
            {"map-b6b657a7-6771-4955-853e-0d4673416559", "r.-1.-1.mca", "r.-1.-2.mca", "r.-1.-3.mca", "r.-1.0.mca", "r.0.-1.mca", "r.0.-2.mca", "r.0.-3.mca", "r.0.0.mca", "r.1.-1.mca"},
            {"map-dbf4ea16-60bd-4a5b-b865-b89377411f49", "r.-1.-1.mca", "r.-1.0.mca", "r.-2.0.mca", "r.-2.1.mca", "r.0.-1.mca", "r.0.0.mca"},
            {"map-e2009967-8e23-4de2-9ff6-683b2b1d1162", "r.-1.-1.mca", "r.-1.0.mca", "r.-1.1.mca", "r.-1.2.mca", "r.-2.-1.mca", "r.-2.0.mca", "r.-2.1.mca", "r.-2.2.mca", "r.-3.0.mca", "r.-3.1.mca", "r.-3.2.mca", "r.0.-1.mca", "r.0.0.mca", "r.0.1.mca"},
        };

        int extracted = 0;
        for (String[] mapEntry : bundledMaps) {
            String mapName = mapEntry[0];
            try {
                File mapDir = new File(mapsFolder, mapName);
                File regionDir = new File(mapDir, "region");
                regionDir.mkdirs();

                // Extrair level.dat
                if (extractResource("maps/" + mapName + "/level.dat", new File(mapDir, "level.dat"))) {
                    // Extrair arquivos de região
                    for (int i = 1; i < mapEntry.length; i++) {
                        String regionFile = mapEntry[i];
                        extractResource("maps/" + mapName + "/region/" + regionFile, new File(regionDir, regionFile));
                    }
                    extracted++;
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to extract map: " + mapName, e);
            }
        }

        plugin.getLogger().info("Extracted " + extracted + " maps successfully");
    }

    /**
     * Extrai um recurso do JAR para um arquivo
     */
    private boolean extractResource(String resourcePath, File outputFile) {
        if (outputFile.exists()) {
            return false; // Já existe
        }

        try {
            outputFile.getParentFile().mkdirs();
            InputStream in = plugin.getResource(resourcePath);
            if (in == null) {
                plugin.getLogger().warning("Resource not found: " + resourcePath);
                return false;
            }

            FileOutputStream out = new FileOutputStream(outputFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            
            in.close();
            out.close();
            
            plugin.getLogger().info("Extracted resource: " + resourcePath);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to extract resource: " + resourcePath, e);
            return false;
        }
    }

    /**
     * Obtém o valor de configuração
     */
    public String getConfigString(String path, String defaultValue) {
        return plugin.getConfig().getString(path, defaultValue);
    }

    /**
     * Obtém o valor de configuração
     */
    public int getConfigInt(String path, int defaultValue) {
        return plugin.getConfig().getInt(path, defaultValue);
    }

    /**
     * Obtém o valor de configuração
     */
    public boolean getConfigBoolean(String path, boolean defaultValue) {
        return plugin.getConfig().getBoolean(path, defaultValue);
    }

    /**
     * Salva a configuração
     */
    public void saveConfig() {
        plugin.saveConfig();
    }

    /**
     * Recarrega a configuração
     */
    public void reloadConfig() {
        plugin.reloadConfig();
    }
}
