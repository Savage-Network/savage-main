package com.minecraft.savagemc.hungergames.command;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe base para comandos do HungerGames
 * Define estrutura e comportamento comum dos comandos
 */
@Getter
public abstract class HungerGamesCommand {
    
    protected final HungerGamesPlugin plugin;
    private final String name;
    private final String description;
    private final String permission;
    private final String usage;
    private final boolean playerOnly;
    
    public HungerGamesCommand(HungerGamesPlugin plugin, String name, String description, String permission) {
        this.plugin = plugin;
        this.name = name;
        this.description = description;
        this.permission = permission;
        this.usage = "/" + name;
        this.playerOnly = false;
    }
    
    public HungerGamesCommand(HungerGamesPlugin plugin, String name, String description, String permission, boolean playerOnly) {
        this.plugin = plugin;
        this.name = name;
        this.description = description;
        this.permission = permission;
        this.usage = "/" + name;
        this.playerOnly = playerOnly;
    }
    
    public HungerGamesCommand(HungerGamesPlugin plugin, String name, String description, String permission, String usage, boolean playerOnly) {
        this.plugin = plugin;
        this.name = name;
        this.description = description;
        this.permission = permission;
        this.usage = usage;
        this.playerOnly = playerOnly;
    }
    
    /**
     * Executa o comando
     */
    public boolean execute(org.bukkit.command.CommandSender sender, String[] args) {
        // Verificar se o sender tem permissão
        if (!sender.hasPermission(permission)) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return false;
        }
        
        // Verificar se é apenas para jogadores
        if (playerOnly && !(sender instanceof org.bukkit.entity.Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores.");
            return false;
        }
        
        try {
            return onCommand(sender, args);
        } catch (Exception e) {
            plugin.getLogger().warning("Error executing command: " + name + " - " + e.getMessage());
            sender.sendMessage("§cOcorreu um erro ao executar o comando.");
            return false;
        }
    }
    
    /**
     * Lógica principal do comando
     */
    protected abstract boolean onCommand(org.bukkit.command.CommandSender sender, String[] args);
    
    /**
     * Completa tab para o comando
     */
    public List<String> tabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        return Arrays.asList();
    }
    
    /**
     * Envia mensagem de ajuda do comando
     */
    protected void sendHelp(org.bukkit.command.CommandSender sender) {
        sender.sendMessage("§e" + description);
        sender.sendMessage("§7Uso: " + usage);
    }
    
    /**
     * Envia mensagem de erro
     */
    protected void sendError(org.bukkit.command.CommandSender sender, String message) {
        sender.sendMessage("§c" + message);
    }
    
    /**
     * Envia mensagem de sucesso
     */
    protected void sendSuccess(org.bukkit.command.CommandSender sender, String message) {
        sender.sendMessage("§a" + message);
    }
    
    /**
     * Envia mensagem de informação
     */
    protected void sendInfo(org.bukkit.command.CommandSender sender, String message) {
        sender.sendMessage("§e" + message);
    }
    
    /**
     * Verifica se um argumento é um número válido
     */
    protected boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Verifica se um jogador está online
     */
    protected org.bukkit.entity.Player getOnlinePlayer(String name) {
        return plugin.getServer().getPlayer(name);
    }
    
    /**
     * Obtém um jogador como sender (se aplicável)
     */
    protected org.bukkit.entity.Player getPlayerSender(org.bukkit.command.CommandSender sender) {
        if (sender instanceof org.bukkit.entity.Player) {
            return (org.bukkit.entity.Player) sender;
        }
        return null;
    }
    
    /**
     * Verifica se um argumento está presente
     */
    protected boolean hasArgs(String[] args, int min) {
        return args != null && args.length >= min;
    }
    
    /**
     * Junta argumentos em uma string
     */
    protected String joinArgs(String[] args, int start) {
        if (args == null || args.length <= start) {
            return "";
        }
        
        return Arrays.stream(Arrays.copyOfRange(args, start, args.length))
                .collect(Collectors.joining(" "));
    }
    
    @Override
    public String toString() {
        return name;
    }
}
