package com.minecraft.savagemc.hungergames.arena;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import lombok.Getter;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerenciador de arenas do HungerGames
 * Controla carregamento, seleção e gerenciamento de arenas
 */
@Getter
public class ArenaManager {
    
    private final HungerGamesPlugin plugin;
    private final Map<String, Arena> arenas;
    private final Map<String, Arena> availableArenas;
    private Arena currentArena;
    private Random random;
    
    public ArenaManager(HungerGamesPlugin plugin) {
        this.plugin = plugin;
        this.arenas = new ConcurrentHashMap<>();
        this.availableArenas = new ConcurrentHashMap<>();
        this.random = new Random();
    }
    
    /**
     * Inicializa o gerenciador de arenas
     */
    public void initialize() {
        plugin.getLogger().info("Arena Manager initialized");
        
        // Carregar arenas do diretório
        loadArenas();
        
        // Validar arenas
        validateArenas();
        
        // Selecionar arena inicial
        selectInitialArena();
    }
    
    /**
     * Carrega todas as arenas do diretório
     */
    private void loadArenas() {
        File arenasFolder = plugin.getConfigManager().getArenasFolder();
        
        if (!arenasFolder.exists() || !arenasFolder.isDirectory()) {
            plugin.getLogger().warning("Arenas folder not found: " + arenasFolder.getAbsolutePath());
            return;
        }
        
        File[] arenaFiles = arenasFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        
        if (arenaFiles == null || arenaFiles.length == 0) {
            plugin.getLogger().warning("No arena files found in: " + arenasFolder.getAbsolutePath());
            return;
        }
        
        int loaded = 0;
        for (File arenaFile : arenaFiles) {
            try {
                Arena arena = loadArenaFromFile(arenaFile);
                if (arena != null) {
                    arenas.put(arena.getName().toLowerCase(), arena);
                    loaded++;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load arena: " + arenaFile.getName() + " - " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Loaded " + loaded + " arenas from " + arenaFiles.length + " files");
    }
    
    /**
     * Carrega uma arena de um arquivo
     */
    private Arena loadArenaFromFile(File file) {
        String arenaName = file.getName().replace(".yml", "");
        
        // Implementar carregamento do arquivo YAML
        // Por enquanto, criar arena padrão
        Arena arena = new Arena(arenaName);
        
        // Configurações padrão
        arena.setDisplayName(arenaName);
        arena.setWorldName("world");
        arena.setMinPlayers(2);
        arena.setMaxPlayers(24);
        arena.setEnabled(true);
        
        return arena;
    }
    
    /**
     * Valida todas as arenas carregadas
     */
    private void validateArenas() {
        int valid = 0;
        int invalid = 0;
        
        for (Arena arena : arenas.values()) {
            if (validateArena(arena)) {
                availableArenas.put(arena.getName().toLowerCase(), arena);
                valid++;
            } else {
                invalid++;
                plugin.getLogger().warning("Invalid arena: " + arena.getName());
            }
        }
        
        plugin.getLogger().info("Arena validation: " + valid + " valid, " + invalid + " invalid");
    }
    
    /**
     * Valida uma arena específica
     */
    private boolean validateArena(Arena arena) {
        // Verificar se o mundo existe
        if (plugin.getServer().getWorld(arena.getWorldName()) == null) {
            plugin.getLogger().warning("World not found for arena: " + arena.getName() + " -> " + arena.getWorldName());
            return false;
        }
        
        // Verificar se tem spawn points
        if (arena.getSpawnPoints().isEmpty()) {
            plugin.getLogger().warning("No spawn points for arena: " + arena.getName());
            return false;
        }
        
        // Verificar se tem configurações válidas
        if (arena.getMinPlayers() < 1 || arena.getMaxPlayers() < arena.getMinPlayers()) {
            plugin.getLogger().warning("Invalid player limits for arena: " + arena.getName());
            return false;
        }
        
        return true;
    }
    
    /**
     * Seleciona a arena inicial
     */
    private void selectInitialArena() {
        if (availableArenas.isEmpty()) {
            plugin.getLogger().warning("No valid arenas available!");
            return;
        }
        
        currentArena = getRandomArena();
        plugin.getLogger().info("Selected initial arena: " + currentArena.getName());
    }
    
    /**
     * Obtém uma arena aleatória disponível
     */
    public Arena getRandomArena() {
        if (availableArenas.isEmpty()) {
            return null;
        }
        
        List<Arena> arenaList = new ArrayList<>(availableArenas.values());
        return arenaList.get(random.nextInt(arenaList.size()));
    }
    
    /**
     * Obtém uma arena específica
     */
    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }
    
    /**
     * Define a arena atual
     */
    public boolean setCurrentArena(String arenaName) {
        Arena arena = availableArenas.get(arenaName.toLowerCase());
        
        if (arena != null) {
            currentArena = arena;
            plugin.getLogger().info("Current arena set to: " + arenaName);
            return true;
        }
        
        return false;
    }
    
    /**
     * Verifica se uma arena está disponível
     */
    public boolean isArenaAvailable(String name) {
        return availableArenas.containsKey(name.toLowerCase());
    }
    
    /**
     * Habilita ou desabilita uma arena
     */
    public void setArenaEnabled(String name, boolean enabled) {
        Arena arena = arenas.get(name.toLowerCase());
        
        if (arena != null) {
            arena.setEnabled(enabled);
            
            if (enabled) {
                // Validar e adicionar às arenas disponíveis
                if (validateArena(arena)) {
                    availableArenas.put(name.toLowerCase(), arena);
                }
            } else {
                // Remover das arenas disponíveis
                availableArenas.remove(name.toLowerCase());
                
                // Se for a arena atual, selecionar outra
                if (currentArena != null && currentArena.getName().equalsIgnoreCase(name)) {
                    selectInitialArena();
                }
            }
        }
    }
    
    /**
     * Recarrega todas as arenas
     */
    public void reloadArenas() {
        plugin.getLogger().info("Reloading arenas...");
        
        // Limpar arenas atuais
        arenas.clear();
        availableArenas.clear();
        currentArena = null;
        
        // Recarregar
        loadArenas();
        validateArenas();
        selectInitialArena();
        
        plugin.getLogger().info("Arena reload completed");
    }
    
    /**
     * Obtém estatísticas das arenas
     */
    public String getStats() {
        return String.format(
            "ArenaManager Stats - Total: %d, Available: %d, Current: %s",
            arenas.size(),
            availableArenas.size(),
            currentArena != null ? currentArena.getName() : "None"
        );
    }
    
    /**
     * Obtém todas as arenas
     */
    public Collection<Arena> getAllArenas() {
        return new ArrayList<>(arenas.values());
    }
    
    /**
     * Obtém todas as arenas disponíveis
     */
    public Collection<Arena> getAvailableArenas() {
        return new ArrayList<>(availableArenas.values());
    }
}
