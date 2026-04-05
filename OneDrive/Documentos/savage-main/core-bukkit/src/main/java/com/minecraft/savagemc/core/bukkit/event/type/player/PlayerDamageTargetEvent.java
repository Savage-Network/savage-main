package com.minecraft.savagemc.core.bukkit.event.type.player;

import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@Getter
@Setter
@AllArgsConstructor
public class PlayerDamageTargetEvent extends EventHandler implements Cancellable {

    private Player player, target;

    private DamageCause cause;
    private double finalDamage;

    private double damage, currentHealth;
    private boolean cancelled;

    public enum DamageCause {
        PROJECTILE, PLAYER
    }

    public boolean isDead() {
        return currentHealth <= 0.05;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    // Métodos adicionais para compatibilidade
    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setCurrentHealth(double currentHealth) {
        this.currentHealth = currentHealth;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public void setTarget(Player target) {
        this.target = target;
    }

    public void setCause(DamageCause cause) {
        this.cause = cause;
    }

    public void setFinalDamage(double finalDamage) {
        this.finalDamage = finalDamage;
    }
}
