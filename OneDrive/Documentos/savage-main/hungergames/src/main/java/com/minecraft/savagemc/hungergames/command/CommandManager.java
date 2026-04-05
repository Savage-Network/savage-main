package com.minecraft.savagemc.hungergames.command;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Gerenciador de comandos do HungerGames
 * Registra e gerencia todos os comandos do plugin
 */
@Getter
public class CommandManager {
    
    private final HungerGamesPlugin plugin;
    private final Map<String, HungerGamesCommand> commands;
    
    public CommandManager(HungerGamesPlugin plugin) {
        this.plugin = plugin;
        this.commands = new HashMap<>();
    }
    
    /**
     * Inicializa o gerenciador de comandos
     */
    public void initialize() {
        plugin.getLogger().info("Command Manager initialized");
        
        // Registrar comandos
        registerCommands();
        
        plugin.getLogger().info("Registered " + commands.size() + " commands");
    }
    
    /**
     * Registra todos os comandos
     */
    private void registerCommands() {
        // Registrar comandos básicos
        registerCommand(new StartCommand(plugin));
        registerCommand(new StopCommand(plugin));
        registerCommand(new ArenaCommand(plugin));
        registerCommand(new KitCommand(plugin));
        registerCommand(new ForcekitCommand(plugin));
        registerCommand(new KitsCommand(plugin));
        registerCommand(new SpectateCommand(plugin));
        registerCommand(new FeastCommand(plugin));
        registerCommand(new StatsCommand(plugin));
        registerCommand(new HelpCommand(plugin));
    }
    
    /**
     * Registra um comando
     */
    public void registerCommand(HungerGamesCommand command) {
        if (command == null) {
            return;
        }
        
        String name = command.getName().toLowerCase();
        commands.put(name, command);
        
        // Registrar no Bukkit
        if (plugin.getCommand(name) == null) {
            // Criar comando dinamicamente se não existir no plugin.yml
            try {
                org.bukkit.command.Command bukkitCommand = new org.bukkit.command.Command(name) {
                    @Override
                    public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                        return command.execute(sender, args);
                    }
                    
                    @Override
                    public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                        return command.tabComplete(sender, args);
                    }
                };
                
                plugin.getServer().getCommandMap().register("hungergames", bukkitCommand);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to register command: " + name + " - " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Registered command: " + name);
    }
    
    /**
     * Desregistra um comando
     */
    public void unregisterCommand(String name) {
        HungerGamesCommand command = commands.remove(name.toLowerCase());
        if (command != null) {
            // Desregistrar do Bukkit
            try {
                plugin.getServer().getCommandMap().getCommand(name).unregister(plugin.getServer().getCommandMap());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to unregister command: " + name);
            }
        }
    }
    
    /**
     * Obtém um comando
     */
    public HungerGamesCommand getCommand(String name) {
        return commands.get(name.toLowerCase());
    }
    
    /**
     * Verifica se um comando está registrado
     */
    public boolean isCommandRegistered(String name) {
        return commands.containsKey(name.toLowerCase());
    }
    
    /**
     * Obtém todos os comandos
     */
    public Map<String, HungerGamesCommand> getAllCommands() {
        return new HashMap<>(commands);
    }
    
    /**
     * Obtém comandos que um jogador pode usar
     */
    public Map<String, HungerGamesCommand> getAvailableCommands(org.bukkit.command.CommandSender sender) {
        Map<String, HungerGamesCommand> available = new HashMap<>();
        
        for (Map.Entry<String, HungerGamesCommand> entry : commands.entrySet()) {
            HungerGamesCommand command = entry.getValue();
            
            if (sender.hasPermission(command.getPermission())) {
                available.put(entry.getKey(), command);
            }
        }
        
        return available;
    }
    
    /**
     * Limpa todos os comandos
     */
    public void clearAllCommands() {
        for (String name : commands.keySet()) {
            unregisterCommand(name);
        }
        commands.clear();
    }
    
    /**
     * Recarrega todos os comandos
     */
    public void reloadCommands() {
        plugin.getLogger().info("Reloading commands...");
        
        clearAllCommands();
        registerCommands();
        
        plugin.getLogger().info("Command reload completed");
    }
}
