package com.minecraft.savagemc.hungergames.listener;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import com.minecraft.savagemc.hungergames.game.state.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;

/**
 * Listener para eventos do mundo no HungerGames
 * Controla entidades, clima e outras interações mundiais
 */
public class WorldListener implements Listener {
    
    private final HungerGamesPlugin plugin;
    
    public WorldListener(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Controlar spawn de criaturas baseado no estado do jogo
        if (plugin.getGameManager().getCurrentGame() == null) {
            // Sem jogo ativo, permitir apenas spawn controlado
            if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
                event.setCancelled(true);
            }
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        // Durante o jogo, permitir spawn de alguns mobs
        if (currentState.isPlaying()) {
            // Permitir apenas mobs hostis durante o jogo
            if (isHostileMob(event.getEntityType())) {
                return; // Permitir spawn
            }
        }
        
        // Cancelar outros spawns
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        // Controlar explosões baseado no estado do jogo
        if (plugin.getGameManager().getCurrentGame() == null) {
            event.setCancelled(true);
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        // Permitir explosões apenas durante o jogo
        if (!currentState.isPlaying()) {
            event.setCancelled(true);
            return;
        }
        
        // Limitar destruição de explosões
        if (event.blockList().size() > 10) {
            event.blockList().clear();
            event.blockList().add(event.getLocation().getBlock());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemSpawn(ItemSpawnEvent event) {
        // Controlar spawn de itens no mundo
        if (plugin.getGameManager().getCurrentGame() == null) {
            event.setCancelled(true);
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        // Permitir itens apenas durante o jogo
        if (!currentState.isPlaying()) {
            event.setCancelled(true);
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onWeatherChange(WeatherChangeEvent event) {
        // Manter clima sempre limpo
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onThunderChange(ThunderChangeEvent event) {
        // Manter sem tempestades
        if (event.toThunderState()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldLoad(WorldLoadEvent event) {
        // Configurar mundo quando carregado
        org.bukkit.World world = event.getWorld();
        
        // Configurar regras do mundo
        world.setAutoSave(false);
        world.setGameRule(org.bukkit.GameRule.RANDOM_TICK_SPEED, 0);
        world.setGameRule(org.bukkit.GameRule.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(org.bukkit.GameRule.LOG_ADMIN_COMMANDS, false);
        world.setGameRule(org.bukkit.GameRule.COMMAND_BLOCK_OUTPUT, false);
        world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(org.bukkit.GameRule.DO_FIRE_TICK, false);
        
        // Definir tempo
        world.setTime(6000L);
        
        // Limpar clima
        world.setStorm(false);
        world.setThundering(false);
        
        // Limpar entidades desnecessárias
        cleanupEntities(world);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityTarget(EntityTargetEvent event) {
        // Controlar targeting de mobs
        if (plugin.getGameManager().getCurrentGame() == null) {
            event.setCancelled(true);
            return;
        }
        
        GameState currentState = plugin.getGameManager().getCurrentGame().getState();
        
        // Mobs não devem atacar durante invencibilidade
        if (currentState == GameState.INVINCIBILITY) {
            event.setCancelled(true);
            return;
        }
        
        // Mobs não devem atacar espectadores
        if (event.getTarget() instanceof org.bukkit.entity.Player) {
            org.bukkit.entity.Player target = (org.bukkit.entity.Player) event.getTarget();
            
            if (plugin.getGameManager().getCurrentGame().isSpectator(target.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        // Este evento é tratado no GameListener, mas adicionamos aqui como backup
        if (!(event.getEntity() instanceof org.bukkit.entity.Player)) {
            return;
        }
        
        // Cancelar mudança de fome para entidades não jogadoras
        event.setCancelled(true);
    }
    
    /**
     * Verifica se um tipo de entidade é um mob hostil
     */
    private boolean isHostileMob(org.bukkit.entity.EntityType type) {
        switch (type) {
            case ZOMBIE:
            case SKELETON:
            case SPIDER:
            case CREEPER:
            case ENDERMAN:
            case WITCH:
            case SLIME:
            case MAGMA_CUBE:
            case GHAST:
            case BLAZE:
            case PIG_ZOMBIE:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Limpa entidades desnecessárias do mundo
     */
    private void cleanupEntities(org.bukkit.World world) {
        world.getEntitiesByClasses(
            org.bukkit.entity.Item.class,
            org.bukkit.entity.FallingBlock.class,
            org.bukkit.entity.ExperienceOrb.class,
            org.bukkit.entity.Arrow.class,
            org.bukkit.entity.Snowball.class,
            org.bukkit.entity.Egg.class,
            org.bukkit.entity.EnderPearl.class
        ).forEach(org.bukkit.entity.Entity::remove);
        
        // Remover mobs indesejados
        world.getEntitiesByClasses(
            org.bukkit.entity.Rabbit.class,
            org.bukkit.entity.Chicken.class,
            org.bukkit.entity.Cow.class,
            org.bukkit.entity.Pig.class,
            org.bukkit.entity.Sheep.class
        ).forEach(entity -> {
            if (!isHostileMob(entity.getType())) {
                entity.remove();
            }
        });
    }
}
