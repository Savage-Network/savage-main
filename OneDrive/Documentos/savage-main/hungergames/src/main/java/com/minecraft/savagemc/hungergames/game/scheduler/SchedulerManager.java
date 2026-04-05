package com.minecraft.savagemc.hungergames.game.scheduler;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Gerenciador de tarefas agendadas do HungerGames
 * Controla tasks periódicas e agendadas do sistema
 */
@Getter
public class SchedulerManager {
    
    private final HungerGamesPlugin plugin;
    private final Map<String, ScheduledTask> tasks;
    private final AtomicBoolean running;
    
    public SchedulerManager(HungerGamesPlugin plugin) {
        this.plugin = plugin;
        this.tasks = new ConcurrentHashMap<>();
        this.running = new AtomicBoolean(false);
    }
    
    /**
     * Inicia o gerenciador de schedulers
     */
    public void start() {
        if (running.get()) {
            return;
        }
        
        running.set(true);
        plugin.getLogger().info("Scheduler Manager started");
        
        // Iniciar tarefas padrão
        startDefaultTasks();
    }
    
    /**
     * Para o gerenciador de schedulers
     */
    public void shutdown() {
        running.set(false);
        
        // Cancelar todas as tarefas
        for (ScheduledTask task : tasks.values()) {
            task.cancel();
        }
        tasks.clear();
        
        plugin.getLogger().info("Scheduler Manager shut down");
    }
    
    /**
     * Inicia tarefas padrão do sistema
     */
    private void startDefaultTasks() {
        // Task de verificação do jogo
        scheduleTask("game_check", new GameCheckTask(plugin), 20L); // A cada segundo
        
        // Task de limpeza
        scheduleTask("cleanup", new CleanupTask(plugin), 12000L); // A cada 10 minutos
        
        // Task de atualização de estatísticas
        scheduleTask("stats_update", new StatsUpdateTask(plugin), 6000L); // A cada 5 minutos
    }
    
    /**
     * Agenda uma tarefa periódica
     */
    public void scheduleTask(String name, Runnable task, long delayTicks) {
        if (tasks.containsKey(name)) {
            cancelTask(name);
        }
        
        int taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
            plugin, 
            task, 
            delayTicks, 
            delayTicks
        );
        
        if (taskId != -1) {
            tasks.put(name, new ScheduledTask(name, taskId, task, delayTicks, true));
            plugin.getLogger().info("Scheduled periodic task: " + name + " (ID: " + taskId + ")");
        } else {
            plugin.getLogger().warning("Failed to schedule periodic task: " + name);
        }
    }
    
    /**
     * Agenda uma tarefa única
     */
    public void scheduleDelayedTask(String name, Runnable task, long delayTicks) {
        if (tasks.containsKey(name)) {
            cancelTask(name);
        }
        
        int taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(
            plugin, 
            task, 
            delayTicks
        );
        
        if (taskId != -1) {
            tasks.put(name, new ScheduledTask(name, taskId, task, delayTicks, false));
            plugin.getLogger().info("Scheduled delayed task: " + name + " (ID: " + taskId + ")");
        } else {
            plugin.getLogger().warning("Failed to schedule delayed task: " + name);
        }
    }
    
    /**
     * Agenda uma tarefa assíncrona
     */
    public void scheduleAsyncTask(String name, Runnable task, long delayTicks) {
        if (tasks.containsKey(name)) {
            cancelTask(name);
        }
        
        int taskId = plugin.getServer().getScheduler().runTaskLaterAsynchronously(
            plugin, 
            () -> {
                task.run();
                tasks.remove(name); // Remover após execução
            }, 
            delayTicks
        ).getTaskId();
        
        if (taskId != -1) {
            tasks.put(name, new ScheduledTask(name, taskId, task, delayTicks, false));
            plugin.getLogger().info("Scheduled async task: " + name + " (ID: " + taskId + ")");
        } else {
            plugin.getLogger().warning("Failed to schedule async task: " + name);
        }
    }
    
    /**
     * Cancela uma tarefa
     */
    public boolean cancelTask(String name) {
        ScheduledTask task = tasks.remove(name);
        
        if (task != null) {
            task.cancel();
            plugin.getLogger().info("Cancelled task: " + name);
            return true;
        }
        
        return false;
    }
    
    /**
     * Verifica se uma tarefa está agendada
     */
    public boolean isTaskScheduled(String name) {
        return tasks.containsKey(name);
    }
    
    /**
     * Obtém uma tarefa agendada
     */
    public ScheduledTask getTask(String name) {
        return tasks.get(name);
    }
    
    /**
     * Obtém todas as tarefas ativas
     */
    public Map<String, ScheduledTask> getAllTasks() {
        return new ConcurrentHashMap<>(tasks);
    }
    
    /**
     * Obtém estatísticas do scheduler
     */
    public String getStats() {
        int periodic = (int) tasks.values().stream().filter(ScheduledTask::isPeriodic).count();
        int delayed = tasks.size() - periodic;
        
        return String.format(
            "Scheduler Stats - Active Tasks: %d (Periodic: %d, Delayed: %d), Running: %s",
            tasks.size(),
            periodic,
            delayed,
            running.get()
        );
    }
    
    /**
     * Classe interna para representar uma tarefa agendada
     */
    public static class ScheduledTask {
        private final String name;
        private final int taskId;
        private final Runnable task;
        private final long delay;
        private final boolean periodic;
        
        public ScheduledTask(String name, int taskId, Runnable task, long delay, boolean periodic) {
            this.name = name;
            this.taskId = taskId;
            this.task = task;
            this.delay = delay;
            this.periodic = periodic;
        }
        
        public void cancel() {
            org.bukkit.Bukkit.getScheduler().cancelTask(taskId);
        }
        
        // Getters
        public String getName() { return name; }
        public int getTaskId() { return taskId; }
        public Runnable getTask() { return task; }
        public long getDelay() { return delay; }
        public boolean isPeriodic() { return periodic; }
    }
}
