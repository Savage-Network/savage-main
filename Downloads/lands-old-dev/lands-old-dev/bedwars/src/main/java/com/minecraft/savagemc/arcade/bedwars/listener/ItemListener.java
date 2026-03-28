package com.minecraft.savagemc.arcade.bedwars.listener;

import com.minecraft.savagemc.arcade.bedwars.BedWars;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.structure.egg.EggBridgeManager;
import com.minecraft.savagemc.arcade.bedwars.structure.egg.EggBridgeTask;
import com.minecraft.savagemc.arcade.bedwars.structure.team.Team;
import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.arcade.room.Room;
import com.minecraft.savagemc.core.arcade.room.map.area.Cuboid;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.room.type.Type;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.savagemc.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.savagemc.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.savagemc.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import com.minecraft.savagemc.core.bukkit.manager.list.CooldownManager;
import com.minecraft.savagemc.core.util.list.StringUtil;
import com.minecraft.savagemc.core.util.list.TimeUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil.consumeItem;
import static com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil.removeItemInHand;

public class ItemListener implements Listener {

    private final BedWars plugin;

    private final String SPAWNED_BY_KEY = "spawnedBy";

    private final List<EntityDamageEvent.DamageCause> causes;

    private final Set<Material> BLOCKS_PERMITTED_TO_EXPLODE = Stream
            .of(Material.ENDER_STONE, Material.WOOD, Material.WOOL, Material.STAINED_CLAY)
            .collect(Collectors.toSet());

    public ItemListener() {
        this.plugin = BedWars.getPlugin(BedWars.class);

        this.causes = Arrays.asList(
                EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
                EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
        );
    }

    /* Handle all utility items */
    @EventHandler
    public void onUtilityItems(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Arena arena = user.getArena();

        Team team = user.getTeam();

        ItemStack hand = player.getItemInHand();

        if (arena.isPhase(RoomPhase.PLAYING) && hand != null && !hand.getType().equals(Material.AIR) && event.getAction().name().contains("RIGHT")) {

            switch (hand.getType()) {
                case COMPASS: {
                    player.performCommand("bussola");
                    break;
                }

                case MAGMA_CREAM: {
                    /* Cooldowns Keys */
                    String SLINGSHOT_COOLDOWN = "Estilingue";

                    if (arena.isType(Type.COMPETITIVE)) return;

//                    if (hasCooldown(player, SLINGSHOT_COOLDOWN)) return;

                    consumeItem(player);

                    Vector vector = player.getEyeLocation().getDirection().multiply(1.1F).setY(1.2F);

                    player.setFallDistance(-1.0F);
                    player.setVelocity(vector);

                    player.playSound(Sound.NOTE_PIANO);

//                    setCooldown(player, SLINGSHOT_COOLDOWN, 5);
                    break;
                }

                case FIREBALL: {
                    if (hasCooldown(player, "Fireball")) return;

                    consumeItem(player);

                    Fireball fireball = player.launchProjectile(Fireball.class);

                    fireball.getWorld().playSound(fireball.getLocation(), Sound.GHAST_FIREBALL, 1, 1);

                    fireball.setYield(0);
                    fireball.setFireTicks(0);
                    fireball.setIsIncendiary(false);

                    setCooldown(player, "Fireball", 1);
                    break;
                }

                case SNOW_BALL: {
                    String MOTH_COOLDOWN_KEY = "Traças";

                    if (hasCooldown(player, MOTH_COOLDOWN_KEY)) {
                        event.setCancelled(true);
                        return;
                    }

                    Snowball snowball = player.launchProjectile(Snowball.class);

                    snowball.setVelocity(snowball.getVelocity().multiply(1.5));

                    snowball.setMetadata(SPAWNED_BY_KEY, new FixedMetadataValue(plugin, team.getPlayers()));

                    setCooldown(player, MOTH_COOLDOWN_KEY, 15);
                    break;
                }

                case MONSTER_EGG: {
                    String GOLEM_COOLDOWN_KEY = "Golem";

                    if (hasCooldown(player, GOLEM_COOLDOWN_KEY)) return;

                    consumeItem(player);

                    IronGolem golem = (IronGolem) player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.IRON_GOLEM);

                    golem.setCustomName("§cDefensor (" + TimeUtil.time(60 * 4) + ")");
                    golem.setCustomNameVisible(true);

                    golem.setMetadata(SPAWNED_BY_KEY, new FixedMetadataValue(plugin, team.getPlayers()));

                    new BukkitRunnable() {
                        int time = 60 * 4;

                        @Override
                        public void run() {
                            time--;

                            if (time <= 0 && golem.isValid()) {
                                golem.remove();

                                cancel();
                                return;
                            }

                            golem.setCustomName("§cDefensor (" + TimeUtil.time(time) + ")");
                        }
                    }.runTaskTimer(plugin, 0, 20);

                    setCooldown(player, GOLEM_COOLDOWN_KEY, 120);
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        if (projectile instanceof Egg) {
            EggBridgeManager.end((Egg) projectile);
        }

        if (projectile instanceof Snowball) {
            Snowball snowball = (Snowball) projectile;

            if (snowball.hasMetadata(SPAWNED_BY_KEY)) {
                List<Player> protectedPlayers = (List<Player>) snowball.getMetadata(SPAWNED_BY_KEY).stream()
                        .findFirst().map(MetadataValue::value).orElse(null);

                // Vendo se a bola de neve ainda é válida, e spawnando traças
                Location location = snowball.getLocation();

                for (int i = 0; i < 3; i++) {
                    Silverfish silverFish = (Silverfish) location.getWorld().spawnEntity(location, EntityType.SILVERFISH);

                    silverFish.setCustomName("§eTraça (" + TimeUtil.time(15) + ")");
                    silverFish.setCustomNameVisible(true);

                    silverFish.setTarget(null);

                    silverFish.setMetadata(SPAWNED_BY_KEY, new FixedMetadataValue(plugin, protectedPlayers));
                    silverFish.setMetadata("ball", new FixedMetadataValue(plugin, snowball));
                }

                new BukkitRunnable() {
                    int time = 15;

                    @Override
                    public void run() {
                        time--;

                        List<Silverfish> entities = new ArrayList<>(snowball.getWorld().getEntitiesByClass(Silverfish.class))
                                .stream()
                                .filter(entity -> isSilverfishFromSnowball(entity, snowball))
                                .collect(Collectors.toList());

                        if (time <= 0) {
                            entities.forEach(Entity::remove);

                            cancel();
                            return;
                        }

                        entities.forEach(entity -> entity.setCustomName("§eTraça (" + TimeUtil.time(time) + ")"));
                    }
                }.runTaskTimer(plugin, 0, 20);
            }
        }

        if (projectile instanceof Fireball) {

            if (event.getHitEntity() instanceof Fireball)
                return;

            World world = projectile.getWorld();

            Location location = projectile.getLocation();

            int yield = 3;

            world.createExplosion(location.getX(), location.getY(), location.getZ(), yield, false, false);

            List<Block> blocks = new ArrayList<>(Cuboid.getBlocksFromCenter(projectile, yield));

            blocks.removeIf(block -> block.getType().isOre());

            EntityExplodeEvent entityExplodeEvent = new EntityExplodeEvent(projectile, location, blocks, yield);

            Bukkit.getPluginManager().callEvent(entityExplodeEvent);
        }
    }

    @SuppressWarnings("unchecked")
    @EventHandler
    public void entityTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player) {
            Entity entity = event.getEntity();

            if (entity.hasMetadata(SPAWNED_BY_KEY)) {
                List<Player> protectedPlayers = (List<Player>) entity.getMetadata(SPAWNED_BY_KEY).stream()
                        .findFirst().map(MetadataValue::value).orElse(null);

                if (protectedPlayers == null) return;

                Player target = (Player) event.getTarget();

                if (protectedPlayers.contains(target))
                    event.setCancelled(true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @EventHandler
    public void entityAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Entity entity = event.getEntity();

            if (entity.hasMetadata(SPAWNED_BY_KEY)) {
                List<Player> protectedPlayers = (List<Player>) entity.getMetadata(SPAWNED_BY_KEY).stream()
                        .findFirst().map(MetadataValue::value).orElse(null);

                if (protectedPlayers == null) return;

                Player target = (Player) event.getDamager();

                if (protectedPlayers.contains(target))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        event.setDroppedExp(0);
    }

    @SuppressWarnings("unchecked")
    @EventHandler
    public void onEntityFindTarget(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (Room arena : BedWars.getManager().getArcade().getArenas()) {
                World world = arena.getWorld();

                if (world != null)
                    for (Entity entity : world.getEntities()) {
                        if (!(entity instanceof Creature) || !entity.hasMetadata(SPAWNED_BY_KEY)) continue;

                        Creature creature = (Creature) entity;

                        List<Player> protectedPlayers = (List<Player>) entity.getMetadata(SPAWNED_BY_KEY).stream()
                                .findFirst().map(MetadataValue::value).orElse(null);

                        if (protectedPlayers == null || protectedPlayers.isEmpty()) continue;

                        // Pegando entidades próximas
                        for (Entity nearbyEntity : entity.getNearbyEntities(10, 10, 10)) {
                            if (!(nearbyEntity instanceof Player)) continue;

                            Player target = (Player) nearbyEntity;

                            User user = (User) User.of(target.getUniqueId());

                            if (user == null || user.isProtected() || !user.inState(ArcadeState.ALIVE)) continue;

                            if (!protectedPlayers.contains(target)) {
                                creature.setTarget(target);
                                break;
                            }
                        }
                    }
            }
        }
    }

    private boolean isSilverfishFromSnowball(Silverfish entity, Snowball snowball) {
        return entity.isValid() && entity.hasMetadata("ball") && entity.getMetadata("ball").stream()
                .findFirst()
                .map(MetadataValue::value)
                .orElse(null) == snowball;
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Arena arena = user.getArena();

        ItemStack item = event.getItem();

        switch (item.getType()) {
            case MILK_BUCKET: {
                event.setCancelled(true);

                user.getContext().setMilkTime(System.currentTimeMillis() + 30000L);

                player.sendMessage("§dLeite Mágico: §cVocê está imune de armadilhas por 30 segundos!");

                removeItemInHand(player);
                break;
            }

            case POTION: {
                int id = item.getDurability();

                event.setCancelled(true);

                switch (id) {
                    // Agilidade
                    case 2: {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 45, 1));

                        removeItemInHand(player);
                        break;
                    }

                    // Super Pulo V
                    case 11: {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 45, 4));

                        removeItemInHand(player);
                        break;
                    }

                    // Invisibilidade
                    case 14: {
                        /* Escondendo armaduras do jogador */
                        for (User arenaUser : arena.getUsers()) {
                            if (arenaUser == null) continue;

                            Player receiver = arenaUser.getAccount().player();

                            if (!arenaUser.isPlayer() || arenaUser.getTeam() != null && !arenaUser.getTeam().equals(user.getTeam())) {
                                ProtocolHandler.hideArmor(player, receiver);
                                ProtocolHandler.hidePlayerName(player, receiver);
                            }
                        }

                        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 30, 0));

                        Savage.getPlatform().runSync(() -> arena.getPlayers().forEach(receiver -> {
                            ProtocolHandler.showArmor(player, receiver);
                            ProtocolHandler.showPlayerName(player, receiver);
                        }), 20 * 30);

                        removeItemInHand(player);
                        break;
                    }
                }

                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvisibilityDamage(PlayerDamageTargetEvent event) {
        Player player = event.getTarget(), damager = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Arena arena = user.getArena();

        if (!event.isCancelled() && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);

            arena.getPlayers().forEach(receiver -> {
                ProtocolHandler.showArmor(player, receiver);
                ProtocolHandler.showPlayerName(player, receiver);
            });
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        ItemStack potion = event.getPotion().getItem();

        if (potion == null || potion.getType().equals(Material.AIR)) return;

        // Lentidão Arremessável
        if (potion.getDurability() == 16394) {
            event.setCancelled(true);

            event.getAffectedEntities().forEach(entity -> entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 5, 1)));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        User user = (User) User.of(event.getPlayer().getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Player player = event.getPlayer();

        Block block = event.getBlock();
        World world = block.getWorld();

        Location location = block.getLocation();

        Arena arena = user.getArena();

        if (!event.isCancelled() && arena.isPhase(RoomPhase.PLAYING)) {
            consumeItem(player);

            if (block.getType().equals(Material.TNT)) {
                Location adjustedLocation = location.clone().add(0.5, 0.5, 0.5);

                // Criar holograma de contagem
                arena.getAliveUsers().forEach(target -> {
                    // Holograma de TNT ativado
                    if (target.getMember().getMetadata().isTntTimerEnabled()) {
                        Player bukkitPlayer = target.getAccount().player();

                        // Renderizar holograma
                        if (bukkitPlayer != null) {
                            HologramClient hologram = BukkitCore.getManager().getHologram().spawnClient(
                                    bukkitPlayer, "tnt-" + StringUtil.generateNumberCode(5),
                                    adjustedLocation.clone().subtract(0, 1.5, 0)
                            );

                            final long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3);

                            hologram.setText(Collections.singletonList("§c" + TimeUtil.formatCooldown(endTime) + "..."));

                            hologram.spawnTo(bukkitPlayer);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    long remainingTime = endTime - System.currentTimeMillis();

                                    if (remainingTime <= 0) {
                                        cancel();
                                        BukkitCore.getManager().getHologram().removeClient(hologram);
                                        return;
                                    }

                                    hologram.setText(0, "§c" + TimeUtil.formatCooldown(endTime) + "...");
                                }
                            }.runTaskTimer(BedWars.getInstance(), 0, 1);
                        }
                    }
                });

                TNTPrimed tntPrimed = world.spawn(adjustedLocation, TNTPrimed.class);

                block.setType(Material.AIR);

                tntPrimed.setIsIncendiary(false);
                tntPrimed.setYield(0);
                tntPrimed.setFuseTicks(60);
                tntPrimed.setFireTicks(0);

                tntPrimed.setVelocity(new Vector(0, 0, 0));
                tntPrimed.teleport(adjustedLocation);

                world.playSound(adjustedLocation, Sound.FUSE, 1F, 0.8F);
            }

            if (arena.isType(Type.CASUAL) && block.getType().equals(Material.SLIME_BLOCK)) {
                if (!block.hasMetadata("jumper"))
                    block.setMetadata("jumper", new FixedMetadataValue(plugin, true));

                ParticleBuilder particle = new ParticleBuilder(ParticleEffect.VILLAGER_HAPPY, location.clone().add(0.5, 0.5, 0.5));

                particle.setSpeed(0.15f)
                        .setAmount(25)
                        .display();

                world.playSound(location, Sound.FALL_BIG, 2, 2f);

                Savage.getPlatform().runSync(() -> block.setType(Material.AIR), 20 * 8);
            }
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        Block block = event.getBlock();

        if (block.hasMetadata("jumper"))
            event.setCancelled(true);
    }

    @EventHandler
    public void jumper(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Arena arena = user.getArena();

        Block down = event.getTo().getBlock().getRelative(BlockFace.DOWN);

        if (arena.isType(Type.CASUAL) && arena.isPhase(RoomPhase.PLAYING)) {

            if (down.getType().equals(Material.SLIME_BLOCK) && down.hasMetadata("jumper")) {
                Vector vector = player.getEyeLocation().getDirection().multiply(1.1F).setY(0.96F);

                player.setFallDistance(-1.0F);
                player.setVelocity(vector);

                player.playSound(Sound.NOTE_PLING);
            }
        }
    }

    @EventHandler
    public void entityDamageEvent(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player && causes.contains(event.getCause()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplodes(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        Location location = event.getLocation();

        World world = location.getWorld();

        Arena arena = (Arena) BukkitCore.getManager().getArcade().getArenas().stream()
                .filter(room -> room.getWorld().equals(world)).findFirst().orElse(null);

        if (entity instanceof TNTPrimed && event.blockList().isEmpty()) {
            int yield = 2;

            world.createExplosion(location.getX(), location.getY(), location.getZ(), yield, false, false);

            List<Block> blocks = new ArrayList<>(Cuboid.getBlocksFromCenter(entity, yield));

            blocks.removeIf(block -> block.getType().isOre());

            EntityExplodeEvent entityExplodeEvent = new EntityExplodeEvent(entity, location, blocks, yield);

            Bukkit.getPluginManager().callEvent(entityExplodeEvent);
        }

        event.setCancelled(true);

        if (arena != null) {
            for (Player player : arena.getPlayers()) {
                if (player.getLocation().distance(location) <= 5) {
                    Vector direction = player.getLocation().toVector().subtract(location.toVector());

                    if (direction.length() > 0)
                        direction.normalize();
                    else
                        direction = new Vector(0, 0, 1);

                    double horizontal = entity instanceof TNTPrimed ? 2.5 : entity instanceof Fireball ? 3.2 : 0;

                    Vector horizontalVelocity = direction.multiply(horizontal);

                    double vertical = entity instanceof TNTPrimed ? 1.1 : entity instanceof Fireball ? 1.080 : 0;

                    horizontalVelocity.setY(vertical);

                    player.setVelocity(horizontalVelocity);
                }
            }

            Savage.getPlatform().runSync(() -> {
                List<Block> blocks = event.blockList()
                        .stream()
                        .filter(block -> BLOCKS_PERMITTED_TO_EXPLODE.contains(block.getType()) && arena.isReversible(block))
                        .limit(Savage.RANDOM.ints(5, 8).findFirst().orElse(5))
                        .collect(Collectors.toList());

                for (Block block : blocks) {
                    if (block.isEmpty()) continue;

                    block.breakNaturally();
                }
            }, 2L);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Egg) {
            Egg egg = (Egg) event.getEntity();

            if (egg.getShooter() instanceof Player) {
                Player shooter = (Player) egg.getShooter();

                User user = (User) User.of(shooter.getUniqueId());

                if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

                EggBridgeManager.save(new EggBridgeTask(egg, shooter, user.getArena(), user.getTeam().getColor()));
            }
        }
    }

    protected boolean hasCooldown(Player player, String name) {
        CooldownManager manager = BukkitCore.getManager().getCooldown();

        if (manager.hasCooldown(player, "item-" + name)) {
            Cooldown cooldown = manager.getCooldown(player, "item-" + name);

            if (cooldown == null) return false;

            player.sendMessage("§cAguarde " + new DecimalFormat("#.#").format(cooldown.getRemaining()) + " para usar " + name + " novamente.");
            return true;
        }

        return false;
    }

    protected void setCooldown(Player player, String name, long time) {
        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Cooldown cooldown = new Cooldown("item-" + name, time);

        cooldown.setShowBar(user.getMember().getMetadata().isShowBar());

        BukkitCore.getManager().getCooldown().addCooldown(player, cooldown);
    }
}
