/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.user.listener;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.translation.Language;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.savagemc.hungergames.game.Game;
import com.minecraft.savagemc.hungergames.game.list.Event;
import com.minecraft.savagemc.hungergames.game.list.events.Scrim;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.object.CombatTag;
import com.minecraft.savagemc.hungergames.user.pattern.Condition;
import com.minecraft.savagemc.hungergames.user.pattern.DieCause;
import com.minecraft.savagemc.hungergames.user.pattern.ItemType;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import com.minecraft.savagemc.hungergames.util.constructor.listener.RecurringListener;
import com.minecraft.savagemc.hungergames.util.game.GameStage;
import com.minecraft.savagemc.hungergames.util.stats.StatsApplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@RecurringListener(register = GameStage.INVINCIBILITY, unregister = GameStage.VICTORY)
public class UserDeath implements Listener, BukkitInterface, Assistance, VariableStorage {

    public UserDeath() {
        loadVariables();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {

        event.setDeathMessage(null);

        User user = getUser(event.getEntity().getUniqueId());

        user.getPlayer().setHealth(20);

        List<ItemStack> drops = new ArrayList<>(event.getDrops());

        event.getDrops().clear();

        CombatTag combatTag = user.getCombatTag();

        User killer = (event.getEntity().getKiller() != null ? getUser(event.getEntity().getKiller().getUniqueId()) : combatTag.isTagged() ? combatTag.getLastHit() : null);

        DieCause dieCause = (killer == null ? DieCause.DIE : (user == killer ? DieCause.SUICIDE : DieCause.KILLED));

        LivingUserDieEvent livingUserDieEvent = new LivingUserDieEvent(user, killer, dieCause != DieCause.SUICIDE, dieCause, drops, user.getPlayer().getLocation());
        livingUserDieEvent.fire();
    }

    @Variable(name = "hg.respawn", announce = true)
    private boolean respawn = true;

    @EventHandler
    public void onLivingUserDie(LivingUserDieEvent event) {

        List<ItemStack> drops = new ArrayList<>(event.getDrops());
        event.getDrops().clear();
        drops.removeIf(drop -> {

            if (drop == null || drop.getType() == Material.AIR)
                return true;

            return hasKey(drop, "undroppable");
        });

        event.setLocationToDrop(event.getLocationToDrop().add(0, 0.2, 0));

        drops.forEach(item -> event.getUser().getPlayer().getWorld().dropItemNaturally(event.getLocationToDrop(), item));
        drops.clear();

        User user = event.getUser();
        User killer = event.getKiller();

        Account account = user.getAccount();

        final boolean hasKiller = killer != null;
        final boolean countStatus = !hasKiller || event.isCountStats() && getGame().getVariables().isCountStats() && !killer.getVictims().contains(user.getUniqueId());

        if (hasKiller)
            killer.addKill();

        if (countStatus) { // Giving statistics.

            if (hasKiller) {
                StatsApplier.KILL.apply(killer);

                for (Kit kit : killer.getKits())
                    kit.appreciate(event);
            }

            StatsApplier.DEATH.apply(user);
        }

        DieCause dieCause = event.getDieCause();
        Game game = getGame();

        if (hasKiller) {
            killer.getPlayer().playSound(killer.getPlayer().getLocation(), Sound.ORB_PICKUP, 4F, 4F);
            killer.addVictim(user.getUniqueId());
            
            // Efeito de raio na morte
            createLightningEffect(user, account, dieCause);

            if (game instanceof Scrim) {
                Scrim scrim = (Scrim) game;
                
                // Verifica se é modo dupla
                if (scrim.getEventMode() == Event.EventMode.DUOS) {
                    // No modo dupla, só aplicar clean se a dupla inteira foi eliminada
                    if (isDuoCompletelyEliminated(user)) {
                        long currentTime = System.currentTimeMillis();
                        long cleanTime = currentTime + 30000L;
                        
                        // Debug: Log dos valores quando clean é definido
                        HungerGames.getInstance().getLogger().info("DEBUG CLEAN SET DUO: killer=" + killer.getPlayer().getName() + ", eliminatedDuo=" + user.getPlayer().getName() + ", currentTime=" + currentTime + ", cleanTime=" + cleanTime);
                        
                        killer.getScrimSettings().setCleanTime(cleanTime);
                    } else {
                        // Debug: Log quando clean NÃO é aplicado
                        HungerGames.getInstance().getLogger().info("DEBUG CLEAN SKIP DUO: killer=" + killer.getPlayer().getName() + ", killed=" + user.getPlayer().getName() + ", duo not completely eliminated");
                    }
                } else {
                    // No modo solo, clean de 40 segundos (padrão)
                    long currentTime = System.currentTimeMillis();
                    long cleanTime = currentTime + 40000L;
                    
                    // Debug: Log dos valores quando clean é definido
                    HungerGames.getInstance().getLogger().info("DEBUG CLEAN SET SOLO: killer=" + killer.getPlayer().getName() + ", currentTime=" + currentTime + ", cleanTime=" + cleanTime);
                    
                    killer.getScrimSettings().setCleanTime(cleanTime);
                }
            }

        }

        user.getPlayer().getOpenInventory().getTopInventory().clear();

        if (account.hasPermission(Rank.VIP) && dieCause.isRespawnable() && user.isOnline()) {
            if (isLateLimit() && respawn) {
                user.setCondition(Condition.ALIVE);
                teleport(user.getPlayer());
                game.die(user);
            } else {
                user.setCondition(Condition.SPECTATOR);
                game.die(user);
                broadcast(event);
            }
        } else {
            if (account.hasPermission(Rank.VIP)) {
                user.setCondition(Condition.SPECTATOR);
                game.die(user);
                broadcast(event);
            } else {
                user.setCondition(Condition.DEAD);
                broadcast(event);
                if (user.isOnline()) {
                    user.setCondition(Condition.SPECTATOR);
                    game.die(user);
                }
            }
        }
    }

    /**
     * Verifica se uma dupla foi completamente eliminada
     */
    private boolean isDuoCompletelyEliminated(User user) {
        // Se o jogador não está em dupla, considera eliminado
        if (user.getDuo() == null) {
            return true;
        }
        
        User duoPartner = user.getDuo();
        
        // Verifica se ambos os membros da dupla estão mortos
        boolean userDead = !user.isAlive();
        boolean partnerDead = !duoPartner.isAlive();
        
        return userDead && partnerDead;
    }

    /**
     * Cria efeito de raio na morte do jogador
     */
    private void createLightningEffect(User user, Account account, DieCause dieCause) {
        Location deathLocation = user.getPlayer().getLocation();
        
        // Verifica se é um jogador VIP com permissão de respawn
        boolean isVipWithRespawn = account.hasPermission(Rank.VIP) && dieCause.isRespawnable();
        
        if (isVipWithRespawn) {
            // Para VIPs com respawn, só criar efeito se o tempo de respawn acabou (após 5 minutos)
            if (!isLateLimit()) {
                // Tempo de respawn acabou - criar efeito de raio
                createLightningAtLocation(deathLocation);
                HungerGames.getInstance().getLogger().info("DEBUG LIGHTNING VIP: player=" + user.getPlayer().getName() + ", time=" + getTime() + ", respawn_time_ended=true");
            } else {
                // Ainda dentro do tempo de respawn - não criar efeito
                HungerGames.getInstance().getLogger().info("DEBUG LIGHTNING SKIP VIP: player=" + user.getPlayer().getName() + ", time=" + getTime() + ", respawn_time_active=true");
            }
        } else {
            // Para jogadores normais ou VIPs sem respawn - sempre criar efeito
            createLightningAtLocation(deathLocation);
            HungerGames.getInstance().getLogger().info("DEBUG LIGHTNING NORMAL: player=" + user.getPlayer().getName() + ", time=" + getTime() + ", isVip=" + account.hasPermission(Rank.VIP));
        }
    }

    /**
     * Cria o efeito visual e sonoro de raio em uma localização específica
     */
    private void createLightningAtLocation(Location location) {
        // Criar raio sem dano (apenas efeito visual)
        location.getWorld().strikeLightningEffect(location);
        
        // Tocar som de raio para todos os jogadores próximos (raio de 50 blocos)
        for (Player nearbyPlayer : location.getWorld().getPlayers()) {
            if (nearbyPlayer.getLocation().distance(location) <= 50) {
                nearbyPlayer.playSound(location, Sound.AMBIENCE_THUNDER, 1.0F, 1.0F);
            }
        }
    }

    public void teleport(Player player) {
        // Pega o tamanho atual da borda (diminui conforme o jogo avança)
        int borderSize = HungerGames.getInstance().getGame().getVariables().getWorldSize();
        
        // Spawna aleatoriamente dentro da borda com margem de segurança de 40 blocos
        int safeRange = borderSize - 40;
        
        // Garante que o range seja positivo
        if (safeRange < 50) {
            safeRange = 50;
        }
        
        // Gera coordenadas aleatórias dentro da borda
        int x = randomize(-safeRange, safeRange);
        int z = randomize(-safeRange, safeRange);
        
        int y = getWorld().getHighestBlockYAt(x, z);
        Location location = new Location(getWorld(), x + 0.5, y, z + 0.5);
        player.teleport(location);
    }

    public void broadcast(LivingUserDieEvent event) {

        String victim = event.getUser().getName() + (event.getDieCause().isNeedKit() ? event.getUser().getKitContainer(true) : "");
        String killer = "";
        Material material = Material.AIR;

        if (event.getKiller() != null) {
            killer = event.getKiller().getName() + (event.getDieCause().isNeedKit() ? event.getKiller().getKitContainer(true) : "");
            material = (event.getKiller().getPlayer().getItemInHand() == null ? Material.AIR : event.getKiller().getPlayer().getItemInHand().getType());
        }

        String brazilian = ItemType.getString(Language.PORTUGUESE, material), english = ItemType.getString(Language.ENGLISH, material);

        int count = (int) getPlugin().getUserStorage().getUsers().stream().filter(User::isAlive).count();

        for (Player p : Bukkit.getOnlinePlayers()) {
            Account account = Account.fetch(p.getUniqueId());
            if (account == null)
                continue;
            Language language = account.getLanguage();
            p.sendMessage(language.translate(event.getDieCause().getMessage(), killer, victim, (language == Language.PORTUGUESE ? brazilian : english)));
            p.sendMessage(language.translate("hg.game.user_death.remaining_players", count));
        }
    }
}
