package com.minecraft.savagemc.core.bukkit.event.type.player;

import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageEvent extends EventHandler implements Cancellable {

    private Player player;
    private EntityDamageEvent.DamageCause cause;

    private double damage, currentHealth;

    private double finalDamage;

    private boolean cancelled;

    public PlayerDamageEvent() {
        this.player = null;
        this.cause = null;
        this.damage = 0;
        this.currentHealth = 0;
        this.finalDamage = 0;
        this.cancelled = false;
    }

    public PlayerDamageEvent(Player player, EntityDamageEvent.DamageCause cause, double damage, double currentHealth, double finalDamage, boolean cancelled) {
        this.player = player;
        this.cause = cause;
        this.damage = damage;
        this.currentHealth = currentHealth;
        this.finalDamage = finalDamage;
        this.cancelled = cancelled;
    }

    // Getters
    public Player getPlayer() {
        return player;
    }

    public EntityDamageEvent.DamageCause getCause() {
        return cause;
    }

    public double getDamage() {
        return damage;
    }

    public double getCurrentHealth() {
        return currentHealth;
    }

    public double getFinalDamage() {
        return finalDamage;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    // Setters
    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setCause(EntityDamageEvent.DamageCause cause) {
        this.cause = cause;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public void setCurrentHealth(double currentHealth) {
        this.currentHealth = currentHealth;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isDead() {
        return currentHealth <= 0.05;
    }
}
