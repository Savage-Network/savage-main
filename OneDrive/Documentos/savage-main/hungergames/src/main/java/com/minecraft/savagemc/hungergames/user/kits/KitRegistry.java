package com.minecraft.savagemc.hungergames.user.kits;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro de kits do HungerGames
 * Gerencia todos os kits disponíveis no sistema
 */
@Getter
public class KitRegistry {
    
    private final Map<String, Kit> kits;
    private final Map<Class<? extends Kit>, Kit> kitsByClass;
    
    public KitRegistry() {
        this.kits = new ConcurrentHashMap<>();
        this.kitsByClass = new ConcurrentHashMap<>();
    }
    
    /**
     * Registra um kit
     */
    public void registerKit(Kit kit) {
        if (kit == null) {
            return;
        }
        
        String name = kit.getName().toLowerCase();
        
        if (kits.containsKey(name)) {
            // Substituir kit existente
            Kit existing = kits.get(name);
            existing.onUnregister();
        }
        
        kits.put(name, kit);
        kitsByClass.put(kit.getClass(), kit);
        
        kit.onRegister();
    }
    
    /**
     * Desregistra um kit
     */
    public void unregisterKit(String name) {
        Kit kit = kits.remove(name.toLowerCase());
        if (kit != null) {
            kit.onUnregister();
            kitsByClass.remove(kit.getClass());
        }
    }
    
    /**
     * Obtém um kit pelo nome
     */
    public Kit getKit(String name) {
        if (name == null) {
            return null;
        }
        return kits.get(name.toLowerCase());
    }
    
    /**
     * Obtém um kit pela classe
     */
    public Kit getKit(Class<? extends Kit> kitClass) {
        return kitsByClass.get(kitClass);
    }
    
    /**
     * Verifica se um kit está registrado
     */
    public boolean isKitRegistered(String name) {
        return kits.containsKey(name.toLowerCase());
    }
    
    /**
     * Obtém todos os kits registrados
     */
    public Collection<Kit> getAllKits() {
        return new ArrayList<>(kits.values());
    }
    
    /**
     * Obtém todos os kits habilitados
     */
    public List<Kit> getEnabledKits() {
        return kits.values().stream()
                .filter(Kit::isEnabled)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Obtém kits por categoria
     */
    public List<Kit> getKitsByCategory(String category) {
        // Implementar quando tiver sistema de categorias
        return getEnabledKits();
    }
    
    /**
     * Obtém o número de kits registrados
     */
    public int getKitCount() {
        return kits.size();
    }
    
    /**
     * Obtém o número de kits habilitados
     */
    public int getEnabledKitCount() {
        return (int) kits.values().stream().filter(Kit::isEnabled).count();
    }
    
    /**
     * Limpa todos os kits
     */
    public void clearAllKits() {
        for (Kit kit : kits.values()) {
            kit.onUnregister();
        }
        kits.clear();
        kitsByClass.clear();
    }
    
    /**
     * Habilita ou desabilita todos os kits
     */
    public void setAllKitsEnabled(boolean enabled) {
        kits.values().forEach(kit -> kit.setEnabled(enabled));
    }
    
    /**
     * Obtém kits aleatórios
     */
    public List<Kit> getRandomKits(int count) {
        List<Kit> enabledKits = getEnabledKits();
        Collections.shuffle(enabledKits);
        
        return enabledKits.stream()
                .limit(Math.min(count, enabledKits.size()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Obtém um kit aleatório
     */
    public Kit getRandomKit() {
        List<Kit> enabledKits = getEnabledKits();
        if (enabledKits.isEmpty()) {
            return null;
        }
        
        Random random = new Random();
        return enabledKits.get(random.nextInt(enabledKits.size()));
    }
    
    /**
     * Busca kits por nome
     */
    public List<Kit> searchKits(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getEnabledKits();
        }
        
        String lowerQuery = query.toLowerCase();
        
        return kits.values().stream()
                .filter(Kit::isEnabled)
                .filter(kit -> kit.getName().toLowerCase().contains(lowerQuery) ||
                              kit.getDisplayName().toLowerCase().contains(lowerQuery))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Obtém estatísticas do registro
     */
    public String getStats() {
        return String.format(
            "KitRegistry Stats - Total Kits: %d, Enabled: %d, Disabled: %d",
            getKitCount(),
            getEnabledKitCount(),
            getKitCount() - getEnabledKitCount()
        );
    }
}
