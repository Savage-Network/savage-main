package com.minecraft.savagemc.hungergames.listener;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import com.minecraft.savagemc.hungergames.game.state.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * Listener para eventos do jogo HungerGames
 * Controla as regras do jogo baseadas no estado atual
 */
public class GameListener implements Listener {
    
    private final HungerGamesPlugin plugin;
    
    public GameListener(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.Player)) {
            return;
        }
        
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getEntity();
        
        // Verificar se há um jogo ativo
        if (plugin.getGameManager().getCurrentGame() == null) {
            event.setCancelled(true);
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        // Cancelar dano se não for permitido no estado atual
        if (currentState.isDamageDisabled()) {
            event.setCancelled(true);
            return;
        }
        
        // Verificar se o jogador está no jogo
        if (!plugin.getGameManager().getCurrentGame().hasPlayer(player.getUniqueId())) {
            // Jogadores fora do jogo não podem receber dano
            event.setCancelled(true);
            return;
        }
        
        // Verificar se o jogador está em invencibilidade
        if (currentState == GameState.INVINCIBILITY) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        
        // Verificar se há um jogo ativo
        if (plugin.getGameManager().getCurrentGame() == null) {
            event.setCancelled(true);
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        // Permitir quebrar blocos apenas durante o jogo
        if (!currentState.allowsBlockBreaking()) {
            event.setCancelled(true);
            return;
        }
        
        // Verificar se o jogador está no jogo
        if (!plugin.getGameManager().getCurrentGame().hasPlayer(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        
        // Verificar se há um jogo ativo
        if (plugin.getGameManager().getCurrentGame() == null) {
            event.setCancelled(true);
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        // Permitir colocar blocos apenas durante o jogo
        if (!currentState.allowsBlockPlacing()) {
            event.setCancelled(true);
            return;
        }
        
        // Verificar se o jogador está no jogo
        if (!plugin.getGameManager().getCurrentGame().hasPlayer(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.Player)) {
            return;
        }
        
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getEntity();
        
        // Verificar se há um jogo ativo
        if (plugin.getGameManager().getCurrentGame() == null) {
            event.setCancelled(true);
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        // Manter fome cheia antes do jogo começar
        if (currentState.isPreGame()) {
            event.setCancelled(true);
            player.setFoodLevel(20);
            return;
        }
        
        // Verificar se o jogador está no jogo
        if (!plugin.getGameManager().getCurrentGame().hasPlayer(player.getUniqueId())) {
            event.setCancelled(true);
            player.setFoodLevel(20);
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        
        // Verificar se há um jogo ativo
        if (plugin.getGameManager().getCurrentGame() == null) {
            event.setCancelled(true);
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        // Não permitir dropar itens antes do jogo começar
        if (currentState.isPreGame()) {
            event.setCancelled(true);
            return;
        }
        
        // Verificar se o jogador está no jogo
        if (!plugin.getGameManager().getCurrentGame().hasPlayer(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        
        // Verificar se há um jogo ativo
        if (plugin.getGameManager().getCurrentGame() == null) {
            event.setCancelled(true);
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        // Não permitir pegar itens antes do jogo começar
        if (currentState.isPreGame()) {
            event.setCancelled(true);
            return;
        }
        
        // Verificar se o jogador está no jogo
        if (!plugin.getGameManager().getCurrentGame().hasPlayer(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
    }
}
