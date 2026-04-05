/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.user.kits.list;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerMassiveTeleportExecuteEvent;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.util.DateUtils;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.savagemc.hungergames.game.list.events.Scrim;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.savagemc.hungergames.user.pattern.DieCause;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import com.minecraft.savagemc.hungergames.util.metadata.GameMetadata;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Gladiator extends Kit {

    private final List<GladiatorSession> gladiatorSessions = new ArrayList<>();

    public Gladiator(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.IRON_FENCE));
        setItems(new ItemFactory(Material.IRON_FENCE).setName("§aDesafiar").setDescription("§7Kit Gladiator").getStack());
        setKitCategory(KitCategory.COMBAT);
        setCooldown(12);
        setPrice(50000);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (isPlayer(event.getRightClicked())) {

            Player player = event.getPlayer();
            Player clicked = (Player) event.getRightClicked();

            if (isUser(player) && isItem(player.getItemInHand())) {

                event.setCancelled(true);

                if (checkInvincibility(player))
                    return;

                if (isCooldown(player)) {
                    dispatchCooldown(player);
                    return;
                }

                User user = User.fetch(clicked.getUniqueId());

                if (!user.isAlive())
                    return;

                if (getKit("Neo").isUser(user)) {
                    player.sendMessage(user.getAccount().getLanguage().translate("kit.neo.not_affected"));
                    return;
                }

                if (isGladiator(player) || isGladiator(clicked))
                    return;

                if (clicked.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                    player.sendMessage("§cAguarde para desafiar " + clicked.getName() + " novamente.");
                    return;
                }

                if (getGame() instanceof Scrim && user.getScrimSettings().isCleanTime()) {
                    player.sendMessage("§cAguarde " + DateUtils.formatDifference(user.getScrimSettings().getCleanTime(), Account.fetch(player.getUniqueId()).getLanguage(), DateUtils.Style.NORMAL) + " para desafiar " + clicked.getName() + " para um duelo.");
                    return;
                }

                // Verificar se o jogador clicado já está em combate
                if (isPlayerInPvPCombat(clicked)) {
                    String combatType = getCombatType(clicked);
                    player.sendMessage("§c" + clicked.getName() + " está em combate e não pode ser desafiado para um duelo no momento.");
                    return;
                }

                // Verificar se o jogador que está puxando já está em combate
                if (isPlayerInPvPCombat(player)) {
                    String combatType = getCombatType(player);
                    player.sendMessage("§cVocê está em combate " + combatType + " e não pode desafiar ninguém para um duelo no momento.");
                    return;
                }

                final Location current = player.getLocation().clone();

                int radius = getGame().getVariables().getWorldSize() - 30;

                if (absolute(current.getX()) > radius || absolute(current.getZ()) > radius)
                    return;

                Location location = current;
                location.setY(140);

                while (!isFree(location)) {
                    location = new Location(getWorld(), randomize(radius), 140, randomize(radius));
                }

                gladiatorSessions.add(new GladiatorSession(getUser(player.getUniqueId()), getUser(clicked.getUniqueId()), location, player.getLocation().clone(), clicked.getLocation().clone(), 210).build().start());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if ((event.getDamager() instanceof Player && event.getEntity() instanceof Player)) {

            Player victim = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            if (isGladiator(victim) && !isGladiator(attacker))
                event.setCancelled(true);
            else if (isGladiator(attacker) && !isGladiator(victim))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL && isUser(event.getPlayer())) {
            if (event.hasItem()) {
                if (isItem(event.getItem())) {
                    event.setCancelled(true);
                    event.getPlayer().updateInventory();
                }
            }
        }
    }

    /*@Variable(name = "hg.kit.gladiator.disable_obsidian", permission = Rank.ADMINISTRATOR)
    private boolean disableObsidian = false;

    @EventHandler
    public void onGladiatorObsidian(LiquidTransformEvent event) {

        if (!disableObsidian)
            return;

        if (event.getBlock().getY() > 128 && event.isObsidian()) {
            event.setCancelled(true);
            event.getBlock().setType(Material.COBBLESTONE);
        }
    }*/

    public boolean isGladiator(Player user) {
        for (GladiatorSession gladiatorSession : gladiatorSessions) {
            if (gladiatorSession.getChallenger().getPlayer().getEntityId() == user.getPlayer().getEntityId() || gladiatorSession.getChallenged().getPlayer().getEntityId() == user.getEntityId())
                return true;
        }
        return false;
    }

    /**
     * Verifica se um jogador está em combate (PvP ou sistema de combate do Scrim)
     */
    private boolean isPlayerInPvPCombat(Player player) {
        // Verifica sistema de combate do Scrim primeiro
        if (isPlayerInScrimCombat(player)) {
            return true;
        }

        // Verifica sistema de combat tag do Hunger Games
        if (isPlayerInHungerGamesCombat(player)) {
            return true;
        }

        // Verifica sistema de PvP
        try {
            // Verifica se o plugin PvP está carregado
            if (Bukkit.getPluginManager().getPlugin("PvP") == null) {
                return false;
            }

            // Usa reflection para acessar a classe User do PvP
            Class<?> pvpUserClass = Class.forName("com.minecraft.pvp.user.User");
            Object pvpUser = pvpUserClass.getMethod("fetch", java.util.UUID.class).invoke(null, player.getUniqueId());

            if (pvpUser != null) {
                // Verifica se o jogador está em combate usando o método inCombat()
                return (Boolean) pvpUserClass.getMethod("inCombat").invoke(pvpUser);
            }
        } catch (Exception e) {
            // Se houver erro, assume que não está em combate PvP
        }

        return false;
    }

    /**
     * Verifica se um jogador está em combate no sistema do Scrim
     */
    private boolean isPlayerInScrimCombat(Player player) {
        try {
            // Verifica se estamos em um evento Scrim
            if (!(getGame() instanceof Scrim)) {
                return false;
            }

            // Usa reflection para acessar o método isInCombat do Scrim
            Class<?> scrimClass = getGame().getClass();
            java.lang.reflect.Method isInCombatMethod = scrimClass.getDeclaredMethod("isInCombat", java.util.UUID.class);
            isInCombatMethod.setAccessible(true);

            return (Boolean) isInCombatMethod.invoke(getGame(), player.getUniqueId());
        } catch (Exception e) {
            // Se houver erro, assume que não está em combate
            return false;
        }
    }

    /**
     * Verifica se um jogador está em combate no sistema de combat tag do Hunger Games
     */
    private boolean isPlayerInHungerGamesCombat(Player player) {
        try {
            User user = User.fetch(player.getUniqueId());
            if (user != null) {
                return user.getCombatTag().isTagged();
            }
        } catch (Exception e) {
            // Se houver erro, assume que não está em combate
        }
        return false;
    }

    /**
     * Identifica o tipo de combate que o jogador está participando
     */
    private String getCombatType(Player player) {
        // Verifica sistema de combate do Scrim
        if (isPlayerInScrimCombat(player)) {
            return "do Scrim";
        }

        // Verifica sistema de combat tag do Hunger Games
        if (isPlayerInHungerGamesCombat(player)) {
            return "do Hunger Games";
        }

        // Verifica sistema de PvP
        try {
            if (Bukkit.getPluginManager().getPlugin("PvP") != null) {
                Class<?> pvpUserClass = Class.forName("com.minecraft.pvp.user.User");
                Object pvpUser = pvpUserClass.getMethod("fetch", java.util.UUID.class).invoke(null, player.getUniqueId());

                if (pvpUser != null && (Boolean) pvpUserClass.getMethod("inCombat").invoke(pvpUser)) {
                    return "PvP";
                }
            }
        } catch (Exception e) {
            // Ignora erros
        }

        return "geral";
    }

    public User getTarget(User user) {
        User response = null;

        for (GladiatorSession gladiatorSession : gladiatorSessions) {

            if (user.getPlayer().getEntityId() == gladiatorSession.getChallenged().getPlayer().getEntityId())
                response = gladiatorSession.getChallenger();

            if (user.getPlayer().getEntityId() == gladiatorSession.getChallenger().getPlayer().getEntityId())
                response = gladiatorSession.getChallenged();
        }

        return response;
    }

    public boolean isFree(Location location) {
        for (GladiatorSession gladiatorSession : this.gladiatorSessions) {
            if (gladiatorSession.getLocation().distanceSquared(location) <= 484)
                return false;
        }
        return true;
    }

    public void unregister(GladiatorSession gladiatorSession) {
        gladiatorSessions.remove(gladiatorSession);
    }

    @Getter
    public class GladiatorSession implements BukkitInterface, Assistance, Listener {

        private final User challenger, challenged;
        private final Location location;
        private Location challengerOldLocation, challengedOldLocation;
        private int time;
        private boolean appliedWither;

        public GladiatorSession(User challenger, User challenged, Location location, Location challengerOldLocation, Location challengedOldLocation, int time) {
            this.challenger = challenger;
            this.challenged = challenged;
            this.location = location;
            this.challengerOldLocation = challengerOldLocation;
            this.challengedOldLocation = challengedOldLocation;
            this.time = time;
        }

        public GladiatorSession build() {
            for (int y = 0; y <= 13; y++) {
                for (int x = -8; x <= 7; x++) {
                    for (int z = -8; z <= 7; z++) {
                        if (y == 0) {
                            Block floor = location.clone().add(x, 0.0d, z).getBlock();
                            floor.setType(Material.GLASS);
                            floor.setMetadata("unbreakable", new GameMetadata(true));
                        }
                        if (x == -8 || x == 7) {
                            Block walls_x = location.clone().add(x, y, z).getBlock();
                            walls_x.setType(Material.GLASS);
                            walls_x.setMetadata("unbreakable", new GameMetadata(true));
                        }

                        if (z == -8 || z == 7) {
                            Block walls_z = location.clone().add(x, y, z).getBlock();
                            walls_z.setType(Material.GLASS);
                            walls_z.setMetadata("unbreakable", new GameMetadata(true));
                        }
                    }
                }
            }
            return this;
        }

        public GladiatorSession start() {

            Bukkit.getPluginManager().registerEvents(this, getPlugin());

            Player player1 = getChallenger().getPlayer();
            Player player2 = getChallenged().getPlayer();

            player1.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 255), false);
            player2.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 255), false);

            // Aplicar combat tag baseado no contexto (Hunger Games ou PvP)
            applyCombatTagBasedOnContext();

            Location first = location.clone().add(-5, 1.2, -5), second = location.clone().add(5, 1.2, 5);

            first.setYaw(-45);
            first.setPitch(0);
            second.setYaw(130);
            second.setPitch(0);

            player1.teleport(first);
            player2.teleport(second);

            return this;
        }

        /**
         * Aplica combat tag baseado no contexto (Hunger Games ou PvP)
         */
        private void applyCombatTagBasedOnContext() {
            // Verifica se estamos em um contexto de PvP
            if (isInPvPContext()) {
                // Usa o sistema de combat tag do PvP
                try {
                    Class<?> pvpUserClass = Class.forName("com.minecraft.pvp.user.User");
                    Object pvpUser1 = pvpUserClass.getMethod("fetch", java.util.UUID.class).invoke(null, getChallenger().getUniqueId());
                    Object pvpUser2 = pvpUserClass.getMethod("fetch", java.util.UUID.class).invoke(null, getChallenged().getUniqueId());

                    if (pvpUser1 != null && pvpUser2 != null) {
                        pvpUserClass.getMethod("addCombat", java.util.UUID.class).invoke(pvpUser1, getChallenged().getUniqueId());
                        pvpUserClass.getMethod("addCombat", java.util.UUID.class).invoke(pvpUser2, getChallenger().getUniqueId());
                    }
                } catch (Exception e) {
                    // Fallback para sistema do Hunger Games se PvP não estiver disponível
                    getChallenger().getCombatTag().addTag(getChallenged(), 15);
                    getChallenged().getCombatTag().addTag(getChallenger(), 15);
                }
            } else {
                // Usa o sistema de combat tag do Hunger Games
                getChallenger().getCombatTag().addTag(getChallenged(), 15);
                getChallenged().getCombatTag().addTag(getChallenger(), 15);
            }
        }

        /**
         * Verifica se estamos em um contexto de PvP
         */
        private boolean isInPvPContext() {
            try {
                // Verifica se o plugin PvP está carregado
                return Bukkit.getPluginManager().getPlugin("PvP") != null;
            } catch (Exception e) {
                return false;
            }
        }

        @EventHandler
        public void onGameTime(ServerHeartbeatEvent event) {

            if (!event.isPeriodic(20))
                return;

            this.time--;

            // Manter combat tag ativo durante todo o duelo baseado no contexto
            if (getChallenger().isOnline() && getChallenged().isOnline()) {
                applyCombatTagBasedOnContext();
            }

            if (this.time == 60) {
                this.appliedWither = true;
                getChallenger().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 1200, 5));
                getChallenged().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 1200, 5));
            } else if (this.time == 0) {
                cancel().undo();
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onLivingUserDieEvent(LivingUserDieEvent event) {
            if (getChallenged().getUniqueId() == event.getUser().getUniqueId()) {
                event.setLocationToDrop(getChallengedOldLocation());
                cancel().undo();
            } else if (getChallenger().getUniqueId() == event.getUser().getUniqueId()) {
                event.setLocationToDrop(getChallengerOldLocation());
                cancel().undo();
            }
        }

        @EventHandler
        public void onMassiveTeleport(PlayerMassiveTeleportExecuteEvent event) { // Fixing massive teleport old Gladiator location.
            this.challengedOldLocation = event.getLocation();
            this.challengerOldLocation = event.getLocation();
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerQuit(PlayerQuitEvent event) {
            if (event.getPlayer().getUniqueId() == getChallenged().getUniqueId()) {
                if (getChallenged().isAlive()) {
                    // Sempre mata o jogador que saiu durante o duelo, independente do combat tag
                    // para evitar exploits de sair durante o duelo
                    new LivingUserDieEvent(getChallenged(), getChallenger(), true, DieCause.COMBAT, getChallenged().getInventoryContents(), getChallengedOldLocation()).fire();
                }
            } else if (event.getPlayer().getUniqueId() == getChallenger().getUniqueId()) {
                if (getChallenger().isAlive()) {
                    // Sempre mata o jogador que saiu durante o duelo, independente do combat tag
                    // para evitar exploits de sair durante o duelo
                    new LivingUserDieEvent(getChallenger(), getChallenged(), true, DieCause.COMBAT, getChallenger().getInventoryContents(), getChallengerOldLocation()).fire();
                }
            }
        }

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            if (event.getPlayer().getUniqueId() == getChallenged().getUniqueId() || event.getPlayer().getUniqueId() == getChallenger().getUniqueId()) {
                if (event.getTo().getY() > 153.3 || event.getTo().getY() < 140.5)
                    cancel().undo();
            }
        }

        public void undo() {
            for (int x = -8; x <= 7; x++) {
                for (int z = -8; z <= 7; z++) {
                    for (int y = 0; y <= 13; y++) {
                        Location location = this.location.clone().add(x, y, z);
                        Block block = location.getBlock();
                        block.setType(Material.AIR);
                        block.removeMetadata("unbreakable", getPlugin());
                    }
                }
            }
        }

        public GladiatorSession cancel() {

            HandlerList.unregisterAll(this);
            unregister(this);

            Player player1 = getChallenger().getPlayer();
            Player player2 = getChallenged().getPlayer();

            player1.teleport(getChallengerOldLocation());
            player2.teleport(getChallengedOldLocation());

            if (getChallenger().isOnline())
                player1.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 255), true);
            if (getChallenged().isOnline())
                player2.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 255), true);

            // Limpar combat tag baseado no contexto
            clearCombatTagBasedOnContext();

            addCooldown(player1.getUniqueId());
            if (isUser(getChallenged()))
                addCooldown(player2.getUniqueId());

            if (appliedWither) {
                player1.removePotionEffect(PotionEffectType.WITHER);
                player2.removePotionEffect(PotionEffectType.WITHER);
            }
            return this;
        }

        /**
         * Limpa combat tag baseado no contexto (Hunger Games ou PvP)
         */
        private void clearCombatTagBasedOnContext() {
            if (isInPvPContext()) {
                // Limpa combat tag do PvP
                try {
                    Class<?> pvpUserClass = Class.forName("com.minecraft.pvp.user.User");
                    Object pvpUser1 = pvpUserClass.getMethod("fetch", java.util.UUID.class).invoke(null, getChallenger().getUniqueId());
                    Object pvpUser2 = pvpUserClass.getMethod("fetch", java.util.UUID.class).invoke(null, getChallenged().getUniqueId());

                    if (pvpUser1 != null && pvpUser2 != null) {
                        pvpUserClass.getMethod("restoreCombat").invoke(pvpUser1);
                        pvpUserClass.getMethod("restoreCombat").invoke(pvpUser2);
                    }
                } catch (Exception e) {
                    // Fallback para sistema do Hunger Games
                    getChallenger().getCombatTag().addTag(null, 0);
                    getChallenged().getCombatTag().addTag(null, 0);
                }
            } else {
                // Limpa combat tag do Hunger Games
                getChallenger().getCombatTag().addTag(null, 0);
                getChallenged().getCombatTag().addTag(null, 0);
            }
        }
    }
}
