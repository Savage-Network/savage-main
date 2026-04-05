package com.minecraft.savagemc.hungergames.command;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;

/**
 * Comando para selecionar kits no HungerGames
 */
public class KitCommand extends HungerGamesCommand {
    
    public KitCommand(HungerGamesPlugin plugin) {
        super(plugin, "kit", "Seleciona um kit para jogar", "hungergames.command.kit", true);
    }
    
    @Override
    protected boolean onCommand(org.bukkit.command.CommandSender sender, String[] args) {
        org.bukkit.entity.Player player = getPlayerSender(sender);
        if (player == null) {
            return false;
        }
        
        if (!hasArgs(args, 1)) {
            sendHelp(sender);
            sendInfo(sender, "Uso: /kit <nome>");
            sendInfo(sender, "Use /kits para ver todos os kits disponíveis.");
            return false;
        }
        
        String kitName = args[0];
        com.minecraft.savagemc.hungergames.user.kits.Kit kit = plugin.getKitManager().getKitRegistry().getKit(kitName);
        
        if (kit == null) {
            sendError(sender, "Kit não encontrado: " + kitName);
            sendInfo(sender, "Use /kits para ver todos os kits disponíveis.");
            return false;
        }
        
        if (!kit.isEnabled()) {
            sendError(sender, "Este kit está desabilitado.");
            return false;
        }
        
        if (!kit.canUse(player)) {
            sendError(sender, "Você não tem permissão para usar este kit.");
            return false;
        }
        
        // Verificar se o jogador pode selecionar kit agora
        com.minecraft.savagemc.hungergames.game.instance.GameInstance currentGame = plugin.getGameManager().getCurrentGame();
        if (currentGame != null) {
            com.minecraft.savagemc.hungergames.game.state.GameState state = currentGame.getState();
            
            if (state.isPlaying()) {
                sendError(sender, "Você não pode selecionar um kit durante o jogo.");
                return false;
            }
        }
        
        // Selecionar o kit
        boolean success = plugin.getKitManager().selectKit(player.getUniqueId(), kitName);
        
        if (success) {
            sendSuccess(sender, "Kit selecionado: " + kit.getDisplayName());
            
            // Mostrar informações do kit
            if (kit.getDescription() != null && !kit.getDescription().isEmpty()) {
                sendInfo(sender, kit.getDescription());
            }
            
            // Ativar callback do kit
            kit.onSelect(player);
        } else {
            sendError(sender, "Falha ao selecionar o kit.");
        }
        
        return true;
    }
    
    @Override
    public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof org.bukkit.entity.Player) {
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
            
            return plugin.getKitManager().getAvailableKits(player.getUniqueId()).stream()
                    .map(com.minecraft.savagemc.hungergames.user.kits.Kit::getName)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        return java.util.Arrays.asList();
    }
}
