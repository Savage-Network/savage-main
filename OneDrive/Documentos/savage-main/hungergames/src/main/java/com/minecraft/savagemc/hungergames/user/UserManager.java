package com.minecraft.savagemc.hungergames.user;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerenciador de usuários do HungerGames
 * Controla dados dos jogadores e sua interação com o jogo
 */
@Getter
public class UserManager {
    
    private final HungerGamesPlugin plugin;
    private final ConcurrentHashMap<UUID, User> onlineUsers;
    
    public UserManager(HungerGamesPlugin plugin) {
        this.plugin = plugin;
        this.onlineUsers = new ConcurrentHashMap<>();
    }
    
    /**
     * Inicializa o gerenciador de usuários
     */
    public void initialize() {
        plugin.getLogger().info("User Manager initialized");
    }
    
    /**
     * Carrega um usuário
     */
    public User loadUser(UUID playerUuid, String playerName) {
        User user = onlineUsers.get(playerUuid);
        
        if (user == null) {
            user = new User(plugin, playerUuid, playerName);
            onlineUsers.put(playerUuid, user);
        }
        
        user.updateName(playerName);
        user.setOnline(true);
        
        return user;
    }
    
    /**
     * Descarrega um usuário
     */
    public void unloadUser(UUID playerUuid) {
        User user = onlineUsers.remove(playerUuid);
        
        if (user != null) {
            user.setOnline(false);
            user.save();
        }
    }
    
    /**
     * Obtém um usuário online
     */
    public User getUser(UUID playerUuid) {
        return onlineUsers.get(playerUuid);
    }
    
    /**
     * Verifica se um usuário está online
     */
    public boolean isUserOnline(UUID playerUuid) {
        return onlineUsers.containsKey(playerUuid);
    }
    
    /**
     * Obtém todos os usuários online
     */
    public java.util.Collection<User> getOnlineUsers() {
        return onlineUsers.values();
    }
    
    /**
     * Obtém o número de usuários online
     */
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }
    
    /**
     * Limpa todos os usuários online
     */
    public void clearOnlineUsers() {
        for (User user : onlineUsers.values()) {
            user.setOnline(false);
            user.save();
        }
        onlineUsers.clear();
    }
    
    /**
     * Salva todos os usuários online
     */
    public void saveAllUsers() {
        for (User user : onlineUsers.values()) {
            user.save();
        }
    }
    
    /**
     * Desliga o gerenciador de usuários
     */
    public void shutdown() {
        plugin.getLogger().info("Shutting down User Manager...");
        
        saveAllUsers();
        clearOnlineUsers();
        
        plugin.getLogger().info("User Manager shut down");
    }
}
