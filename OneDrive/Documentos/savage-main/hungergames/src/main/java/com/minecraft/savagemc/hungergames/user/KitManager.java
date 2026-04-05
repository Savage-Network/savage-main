package com.minecraft.savagemc.hungergames.user;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.KitRegistry;
import com.minecraft.savagemc.hungergames.user.kits.list.NoKit;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gerenciador de kits do HungerGames
 * Controla registro, distribuição e gerenciamento de kits
 */
@Getter
public class KitManager {
    
    private final HungerGamesPlugin plugin;
    private final KitRegistry kitRegistry;
    private final Map<UUID, Kit> playerKits;
    private Kit defaultKit;
    
    public KitManager(HungerGamesPlugin plugin) {
        this.plugin = plugin;
        this.kitRegistry = new KitRegistry();
        this.playerKits = new HashMap<>();
    }
    
    /**
     * Inicializa o gerenciador de kits
     */
    public void initialize() {
        plugin.getLogger().info("Kit Manager initialized");
        
        // Registrar kits padrão
        registerDefaultKits();
        
        // Carregar configurações de kits
        loadKitConfigurations();
    }
    
    /**
     * Registra os kits padrão
     */
    private void registerDefaultKits() {
        // Registrar kit padrão (sem kit)
        defaultKit = new NoKit(plugin);
        kitRegistry.registerKit(defaultKit);
        
        // Registrar todos os kits do HungerGames
        registerAllHungerGamesKits();
        
        plugin.getLogger().info("Registered " + kitRegistry.getKitCount() + " kits");
    }
    
    /**
     * Registra todos os kits do HungerGames
     */
    private void registerAllHungerGamesKits() {
        // Lista de todos os 85 kits
        String[] kitClasses = {
            "Achilles", "Anchor", "AntiTower", "Archer", "Assassin", "AutoBowl",
            "Backpacker", "Barbarian", "Beastmaster", "Berserker", "Blacksmith", "Blink",
            "Boxer", "Camel", "Cannibal", "Chameleon", "Checkpoint", "Cookiemonster",
            "Crafter", "Cultivator", "Demoman", "Digger", "Endermage", "Expel", "Explorer",
            "Fireman", "Fisherman", "Flash", "Forger", "Gladiator", "Grandpa", "Grappler",
            "Hulk", "Ironman", "Jackhammer", "Jumper", "Kangaroo", "Kaya", "Launcher",
            "Lumberjack", "Magic", "Magma", "Milkman", "Miner", "Monk", "Monkey",
            "Neo", "Ninja", "Noob", "Phantom", "Poseidon", "PowerWave", "Pyro",
            "Reaper", "Redstoner", "Rider", "Sabotager", "SandMan", "Scout", "Snail",
            "Specialist", "Spider", "Stomper", "Surprise", "Switcher", "Tank", "Thermo",
            "Thor", "Timelord", "Toxic", "Trader", "Turtle", "Urgal", "Vacuum",
            "Vampire", "Viking", "Viper", "Werewolf", "Worm"
        };
        
        int registered = 0;
        for (String kitName : kitClasses) {
            try {
                // Tentar registrar versão Modern primeiro, depois a versão normal
                Kit modernKit = createKit(kitName + "Modern");
                if (modernKit != null) {
                    kitRegistry.registerKit(modernKit);
                    registered++;
                    continue;
                }
                
                // Se não tiver versão Modern, registrar a versão normal
                Kit normalKit = createKit(kitName);
                if (normalKit != null) {
                    kitRegistry.registerKit(normalKit);
                    registered++;
                }
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to register kit: " + kitName + " - " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Successfully registered " + registered + " HungerGames kits");
    }
    
    /**
     * Cria uma instância de kit pelo nome da classe
     */
    private Kit createKit(String className) {
        try {
            String fullClassName = "com.minecraft.savagemc.hungergames.user.kits.list." + className;
            Class<?> kitClass = Class.forName(fullClassName);
            
            if (Kit.class.isAssignableFrom(kitClass)) {
                return (Kit) kitClass.getConstructor(HungerGamesPlugin.class).newInstance(plugin);
            }
        } catch (Exception e) {
            // Silently fail - kit não encontrado ou erro ao criar
        }
        
        return null;
    }
    
    /**
     * Carrega configurações dos kits
     */
    private void loadKitConfigurations() {
        // Carregar configurações de enable/disable, preços, etc.
        // Por enquanto, todos os kits estão habilitados
    }
    
    /**
     * Seleciona um kit para um jogador
     */
    public boolean selectKit(UUID playerUuid, String kitName) {
        Kit kit = kitRegistry.getKit(kitName);
        
        if (kit == null) {
            return false;
        }
        
        // Verificar permissões
        if (!hasKitPermission(playerUuid, kit)) {
            return false;
        }
        
        playerKits.put(playerUuid, kit);
        return true;
    }
    
    /**
     * Obtém o kit de um jogador
     */
    public Kit getPlayerKit(UUID playerUuid) {
        return playerKits.getOrDefault(playerUuid, defaultKit);
    }
    
    /**
     * Remove o kit de um jogador
     */
    public void removePlayerKit(UUID playerUuid) {
        playerKits.remove(playerUuid);
    }
    
    /**
     * Verifica se um jogador tem permissão para usar um kit
     */
    public boolean hasKitPermission(UUID playerUuid, Kit kit) {
        // Verificar permissão baseada no rank
        String permission = "hungergames.kit." + kit.getName().toLowerCase();
        return plugin.hasPermission(playerUuid, permission);
    }
    
    /**
     * Verifica se um jogador pode comprar um kit
     */
    public boolean canBuyKit(UUID playerUuid, Kit kit) {
        // Implementar lógica de compra com moedas/vip
        // Por enquanto, apenas verifica permissão
        return hasKitPermission(playerUuid, kit);
    }
    
    /**
     * Dá os itens do kit para um jogador
     */
    public void giveKitItems(UUID playerUuid) {
        Kit kit = getPlayerKit(playerUuid);
        org.bukkit.entity.Player player = plugin.getServer().getPlayer(playerUuid);
        
        if (player != null && player.isOnline()) {
            kit.giveItems(player);
        }
    }
    
    /**
     * Remove os itens do kit de um jogador
     */
    public void removeKitItems(UUID playerUuid) {
        Kit kit = getPlayerKit(playerUuid);
        org.bukkit.entity.Player player = plugin.getServer().getPlayer(playerUuid);
        
        if (player != null && player.isOnline()) {
            kit.removeItems(player);
        }
    }
    
    /**
     * Limpa todos os kits de jogadores
     */
    public void clearAllPlayerKits() {
        playerKits.clear();
    }
    
    /**
     * Obtém todos os kits disponíveis para um jogador
     */
    public java.util.List<Kit> getAvailableKits(UUID playerUuid) {
        return kitRegistry.getAllKits().stream()
                .filter(kit -> hasKitPermission(playerUuid, kit))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Habilita ou desabilita um kit
     */
    public void setKitEnabled(String kitName, boolean enabled) {
        Kit kit = kitRegistry.getKit(kitName);
        if (kit != null) {
            kit.setEnabled(enabled);
        }
    }
    
    /**
     * Verifica se um kit está habilitado
     */
    public boolean isKitEnabled(String kitName) {
        Kit kit = kitRegistry.getKit(kitName);
        return kit != null && kit.isEnabled();
    }
    
    /**
     * Obtém estatísticas dos kits
     */
    public String getKitStats() {
        return String.format(
            "KitManager Stats - Registered Kits: %d, Players with Kits: %d, Default Kit: %s",
            kitRegistry.getKitCount(),
            playerKits.size(),
            defaultKit.getName()
        );
    }
}
