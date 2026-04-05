package com.minecraft.savagemc.hungergames.arena;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa uma arena do HungerGames
 * Contém informações sobre o mapa, spawns e configurações
 */
@Getter
@Setter
public class Arena {
    
    private String name;
    private String displayName;
    private String worldName;
    private String author;
    private String description;
    private boolean enabled;
    private int minPlayers;
    private int maxPlayers;
    private List<Location> spawnPoints;
    private Location feastLocation;
    private Location minifeastLocation;
    private UUID arenaId;
    private long createdAt;
    private long lastUsed;
    
    // Configurações de jogo
    private boolean allowBreakBlocks;
    private boolean allowPlaceBlocks;
    private boolean allowPvP;
    private boolean invincibilityEnabled;
    private long invincibilityDuration;
    private long feastTime;
    private boolean borderEnabled;
    private int borderStartSize;
    private int borderEndSize;
    private long borderShrinkTime;
    
    public Arena(String name) {
        this.name = name;
        this.displayName = name;
        this.worldName = "world";
        this.author = "Unknown";
        this.description = "Arena " + name;
        this.enabled = true;
        this.minPlayers = 2;
        this.maxPlayers = 24;
        this.spawnPoints = new ArrayList<>();
        this.arenaId = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.lastUsed = 0;
        
        // Configurações padrão
        this.allowBreakBlocks = true;
        this.allowPlaceBlocks = true;
        this.allowPvP = true;
        this.invincibilityEnabled = true;
        this.invincibilityDuration = 60000; // 1 minuto
        this.feastTime = 300000; // 5 minutos
        this.borderEnabled = true;
        this.borderStartSize = 500;
        this.borderEndSize = 100;
        this.borderShrinkTime = 600000; // 10 minutos
    }
    
    /**
     * Adiciona um ponto de spawn
     */
    public void addSpawnPoint(Location location) {
        if (location != null && !spawnPoints.contains(location)) {
            spawnPoints.add(location.clone());
        }
    }
    
    /**
     * Remove um ponto de spawn
     */
    public boolean removeSpawnPoint(Location location) {
        return spawnPoints.remove(location);
    }
    
    /**
     * Obtém um ponto de spawn aleatório
     */
    public Location getRandomSpawnPoint() {
        if (spawnPoints.isEmpty()) {
            return null;
        }
        
        int index = (int) (Math.random() * spawnPoints.size());
        return spawnPoints.get(index).clone();
    }
    
    /**
     * Obtém um ponto de spawn específico
     */
    public Location getSpawnPoint(int index) {
        if (index < 0 || index >= spawnPoints.size()) {
            return null;
        }
        
        return spawnPoints.get(index).clone();
    }
    
    /**
     * Obtém o mundo da arena
     */
    public World getWorld() {
        return org.bukkit.Bukkit.getWorld(worldName);
    }
    
    /**
     * Verifica se a arena é válida
     */
    public boolean isValid() {
        return enabled && 
               !spawnPoints.isEmpty() && 
               minPlayers > 0 && 
               maxPlayers >= minPlayers &&
               getWorld() != null;
    }
    
    /**
     * Verifica se a arena suporta um número de jogadores
     */
    public boolean supportsPlayerCount(int playerCount) {
        return playerCount >= minPlayers && playerCount <= maxPlayers;
    }
    
    /**
     * Obtém o número ideal de spawns para o número de jogadores
     */
    public int getIdealSpawnCount(int playerCount) {
        return Math.min(playerCount, spawnPoints.size());
    }
    
    /**
     * Atualiza o tempo de último uso
     */
    public void updateLastUsed() {
        this.lastUsed = System.currentTimeMillis();
    }
    
    /**
     * Obtém a localização central da arena
     */
    public Location getCenterLocation() {
        if (spawnPoints.isEmpty()) {
            World world = getWorld();
            if (world != null) {
                return world.getSpawnLocation();
            }
            return null;
        }
        
        // Calcular centro médio dos spawns
        double x = 0, y = 0, z = 0;
        World world = null;
        
        for (Location spawn : spawnPoints) {
            x += spawn.getX();
            y += spawn.getY();
            z += spawn.getZ();
            if (world == null) {
                world = spawn.getWorld();
            }
        }
        
        int count = spawnPoints.size();
        return new Location(world, x / count, y / count, z / count);
    }
    
    /**
     * Verifica se uma localização está dentro da arena
     */
    public boolean isInsideArena(Location location) {
        if (!borderEnabled) {
            return true; // Sem borda, considera tudo dentro
        }
        
        Location center = getCenterLocation();
        if (center == null) {
            return false;
        }
        
        // Verificar se está no mesmo mundo
        if (!center.getWorld().equals(location.getWorld())) {
            return false;
        }
        
        // Verificar distância do centro
        double distance = center.distance(location);
        return distance <= borderStartSize;
    }
    
    /**
     * Reseta a arena para estado inicial
     */
    public void reset() {
        lastUsed = 0;
        // Limpar outros estados se necessário
    }
    
    @Override
    public String toString() {
        return displayName != null ? displayName : name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Arena arena = (Arena) obj;
        return name.equals(arena.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
