package com.minecraft.savagemc.hungergames.listener;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import com.minecraft.savagemc.hungergames.game.state.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Listener para eventos de combate no HungerGames
 * Controla PvP, uso de armas e interações de combate
 */
public class CombatListener implements Listener {
    
    private final HungerGamesPlugin plugin;
    
    public CombatListener(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Verificar se ambos são jogadores (PvP)
        if (!(event.getEntity() instanceof org.bukkit.entity.Player) || 
            !(event.getDamager() instanceof org.bukkit.entity.Player)) {
            return;
        }
        
        org.bukkit.entity.Player victim = (org.bukkit.entity.Player) event.getEntity();
        org.bukkit.entity.Player damager = (org.bukkit.entity.Player) event.getDamager();
        
        // Verificar se há um jogo ativo
        if (plugin.getGameManager().getCurrentGame() == null) {
            event.setCancelled(true);
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        // Permitir PvP apenas durante estados que permitem
        if (!currentState.allowsPvP()) {
            event.setCancelled(true);
            damager.sendMessage("§cPvP não permitido neste momento!");
            return;
        }
        
        // Verificar se ambos estão jogando
        if (!plugin.getGameManager().getCurrentGame().hasPlayer(victim.getUniqueId()) ||
            !plugin.getGameManager().getCurrentGame().hasPlayer(damager.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        
        // Verificar invencibilidade individual
        if (hasInvincibility(victim)) {
            event.setCancelled(true);
            damager.sendMessage("§cEste jogador está invencível!");
            return;
        }
        
        if (hasInvincibility(damager)) {
            event.setCancelled(true);
            damager.sendMessage("§cVocê está invencível!");
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        
        // Verificar se há um jogo ativo
        if (plugin.getGameManager().getCurrentGame() == null) {
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        // Verificar se o jogador está no jogo
        if (!plugin.getGameManager().getCurrentGame().hasPlayer(player.getUniqueId())) {
            return;
        }
        
        // Controlar uso de itens baseado no estado
        if (currentState.isPreGame()) {
            // Não permitir usar itens antes do jogo começar
            if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR ||
                event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                
                if (event.getItem() != null && event.getItem().getType() != Material.AIR) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        
        // Verificar se há um jogo ativo
        if (plugin.getGameManager().getCurrentGame() == null) {
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        // Verificar se o jogador está no jogo
        if (!plugin.getGameManager().getCurrentGame().hasPlayer(player.getUniqueId())) {
            return;
        }
        
        // Controlar interações com entidades baseado no estado
        if (currentState.isPreGame()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityShootBow(EntityShootBowEvent event) {
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
        
        // Verificar se o jogador está no jogo
        if (!plugin.getGameManager().getCurrentGame().hasPlayer(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        
        // Permitir arcos apenas durante o jogo
        if (!currentState.allowsPvP()) {
            event.setCancelled(true);
            player.sendMessage("§cVocê não pode usar arcos neste momento!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPotionSplash(PotionSplashEvent event) {
        // Verificar se há um jogo ativo
        if (plugin.getGameManager().getCurrentGame() == null) {
            event.setCancelled(true);
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        // Permitir poções apenas durante o jogo
        if (!currentState.allowsPvP()) {
            event.setCancelled(true);
            return;
        }
        
        // Verificar afetados
        for (org.bukkit.entity.LivingEntity affected : event.getAffectedEntities()) {
            if (affected instanceof org.bukkit.entity.Player) {
                org.bukkit.entity.Player player = (org.bukkit.entity.Player) affected;
                
                // Não afetar jogadores que não estão no jogo
                if (!plugin.getGameManager().getCurrentGame().hasPlayer(player.getUniqueId())) {
                    event.setIntensity(affected, 0);
                    continue;
                }
                
                // Não afetar jogadores invencíveis
                if (hasInvincibility(player)) {
                    event.setIntensity(affected, 0);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerFish(PlayerFishEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        
        // Verificar se há um jogo ativo
        if (plugin.getGameManager().getCurrentGame() == null) {
            event.setCancelled(true);
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        // Verificar se o jogador está no jogo
        if (!plugin.getGameManager().getCurrentGame().hasPlayer(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        
        // Controlar pesca baseado no estado
        if (currentState.isPreGame()) {
            event.setCancelled(true);
            return;
        }
        
        // Controlar se pode puxar outros jogadores
        if (event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY && 
            event.getCaught() instanceof org.bukkit.entity.Player) {
            
            org.bukkit.entity.Player caught = (org.bukkit.entity.Player) event.getCaught();
            
            // Permitir puxar apenas se ambos estiverem jogando e PvP permitido
            if (!currentState.allowsPvP() || 
                !plugin.getGameManager().getCurrentGame().hasPlayer(caught.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Verifica se um jogador tem invencibilidade
     */
    private boolean hasInvincibility(org.bukkit.entity.Player player) {
        // Verificar invencibilidade do estado do jogo
        if (plugin.getGameManager().getCurrentGame() != null) {
            GameState currentState = plugin.getGameManager().getCurrentGame().getState();
            if (currentState == GameState.INVINCIBILITY) {
                return true;
            }
        }
        
        // Verificar efeitos de poção de invencibilidade
        return player.hasPotionEffect(org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE);
    }
}
