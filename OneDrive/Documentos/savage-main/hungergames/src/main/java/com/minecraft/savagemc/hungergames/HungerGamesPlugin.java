package com.minecraft.savagemc.hungergames;

import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.account.Account;
import java.util.UUID;

import org.bukkit.Particle;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import com.minecraft.savagemc.hungergames.arena.ArenaManager;
import com.minecraft.savagemc.hungergames.command.CommandManager;
import com.minecraft.savagemc.hungergames.config.ConfigurationManager;
import com.minecraft.savagemc.hungergames.game.GameManager;
import com.minecraft.savagemc.hungergames.game.scheduler.SchedulerManager;
import com.minecraft.savagemc.hungergames.listener.EventManager;
import com.minecraft.savagemc.hungergames.user.KitManager;
import com.minecraft.savagemc.hungergames.user.UserManager;
import com.minecraft.savagemc.hungergames.user.celebrations.CelebrationManager;
import lombok.Getter;

/**
 * Plugin principal do HungerGames SavageMC
 * Sistema completo de jogos HungerGames com arquitetura moderna
 */
@Getter
public class HungerGamesPlugin extends BukkitCore {

    private ConfigurationManager configManager;
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private UserManager userManager;
    private KitManager kitManager;
    private CelebrationManager celebrationManager;
    private CommandManager commandManager;
    private EventManager eventManager;
    private SchedulerManager schedulerManager;

    @Override
    public void onLoad() {
        super.onLoad();
        getLogger().info("Loading SavageMC HungerGames Plugin...");
    }

    @Override
    public void onEnable() {
        getLogger().info("Enabling SavageMC HungerGames Plugin...");

        try {
            initializeManagers();
            startManagers();
            configureWorld();
            extractDefaultResources();

            getLogger().info("SavageMC HungerGames Plugin enabled successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to enable plugin: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling SavageMC HungerGames Plugin...");

        if (schedulerManager != null) {
            schedulerManager.shutdown();
        }

        if (gameManager != null) {
            gameManager.shutdown();
        }

        if (userManager != null) {
            userManager.shutdown();
        }

        getLogger().info("SavageMC HungerGames Plugin disabled!");
    }

    /**
     * Inicializa todos os gerenciadores do sistema
     */
    private void initializeManagers() {
        configManager = new ConfigurationManager(this);
        arenaManager = new ArenaManager(this);
        gameManager = new GameManager(this);
        userManager = new UserManager(this);
        kitManager = new KitManager(this);
        celebrationManager = new CelebrationManager(this);
        commandManager = new CommandManager(this);
        eventManager = new EventManager(this);
        schedulerManager = new SchedulerManager(this);
    }

    /**
     * Inicia todos os gerenciadores na ordem correta
     */
    private void startManagers() {
        configManager.initialize();
        arenaManager.initialize();
        kitManager.initialize();
        celebrationManager.initialize();
        userManager.initialize();
        gameManager.initialize();

        commandManager.initialize();
        eventManager.initialize();
        schedulerManager.start();
    }

    /**
     * Configura o mundo do jogo
     */
    private void configureWorld() {
        if (getServer().getWorlds().isEmpty()) {
            getLogger().warning("No worlds found!");
            return;
        }

        org.bukkit.World world = getServer().getWorlds().get(0);
        
        // Limpar entidades desnecessárias
        world.getEntitiesByClasses(org.bukkit.entity.Item.class, 
                                   org.bukkit.entity.FallingBlock.class, 
                                   org.bukkit.entity.Rabbit.class)
             .forEach(org.bukkit.entity.Entity::remove);

        // Configurar regras do mundo
        world.setAutoSave(false);
        world.setGameRule(org.bukkit.GameRule.RANDOM_TICK_SPEED, 0);
        world.setGameRule(org.bukkit.GameRule.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(org.bukkit.GameRule.LOG_ADMIN_COMMANDS, false);
        world.setGameRule(org.bukkit.GameRule.COMMAND_BLOCK_OUTPUT, false);
        world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setTime(6000L);
    }

    /**
     * Extrai recursos padrão (estruturas, mapas)
     */
    private void extractDefaultResources() {
        configManager.extractDefaultStructures();
        configManager.extractDefaultMap();
    }

    /**
     * Obtém uma conta do jogador de forma segura
     */
    public Account getAccount(UUID uuid) {
        return com.minecraft.savagemc.core.Savage.getAccountData().of(uuid);
    }

    /**
     * Obtém uma conta do jogador de forma segura
     */
    public Account getAccount(String name) {
        return com.minecraft.savagemc.core.Savage.getAccountData().of(name);
    }

    /**
     * Verifica se um jogador tem permissão baseada no rank
     */
    public boolean hasPermission(UUID uuid, String permission) {
        Account account = getAccount(uuid);
        return account != null && account.hasPermission(permission);
    }

    /**
     * Verifica se um jogador tem rank específico
     */
    public boolean hasRank(UUID uuid, com.minecraft.savagemc.core.account.context.objects.rank.type.RankType rankType) {
        Account account = getAccount(uuid);
        return account != null && account.hasRank(rankType);
    }
}
