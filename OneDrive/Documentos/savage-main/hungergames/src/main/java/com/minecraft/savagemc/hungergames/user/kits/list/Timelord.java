package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.listener.DynamicListener;
import com.minecraft.core.bukkit.util.particle.ParticleBuilder;
import com.minecraft.core.bukkit.util.particle.ParticleEffect;
import com.minecraft.core.bukkit.util.particle.data.color.RegularColor;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class Timelord extends Kit {

    public Timelord(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.WATCH));
        setItems(new ItemFactory(Material.WATCH).setName("§aStopwatch").setDescription("§7Kit Timelord").getStack());
        setCooldown(32);
        setKitCategory(KitCategory.STRATEGY);
        setPrice(40000);
    }

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!ACCEPTABLES_INTERACT.contains(event.getAction()))
            return;

        if (!isUser(player))
            return;

        if (!isItem(player.getItemInHand()))
            return;

        if (checkInvincibility(player))
            return;

        if (isCooldown(player)) {
            dispatchCooldown(player);
            return;
        }

        addCooldown(player.getUniqueId());

        TimeStop timeStop = new TimeStop(getUser(player.getUniqueId()));
        timeStop.start();
    }

    @Getter
    public class TimeStop extends DynamicListener implements Assistance {

        private final User owner;
        private final Location center;
        private final Set<Player> stuck = new HashSet<>();
        private final Set<Player> free = new HashSet<>();

        public TimeStop(User user) {
            this.owner = user;
            this.center = user.getPlayer().getLocation();
        }

        private void start() {
            register();

            new BukkitRunnable() {

                int time = 6;

                public void run() {

                    if (time-- < 0) {
                        cancel();
                        stop();
                        return;
                    }

                    if (time % 2 == 0)
                        async(() -> makeAnimation());

                    getPlugin().getUserStorage().getAliveUsers().forEach(user -> {

                        if (user.getPlayer().getEntityId() == owner.getPlayer().getEntityId())
                            return;

                        Player player = user.getPlayer();

                        if (player.getLocation().distance(center) < 8) {

                            if (user.hasTeam() && user.getTeam().isMember(owner))
                                return;

                            if (free.contains(player))
                                return;

                            if (!stuck.contains(player)) {
                                stuck.add(player);
                                player.sendMessage("§eVocê foi congelado por um Timelord!");
                                player.sendMessage("§cAguarde " + (time / 2) + " segundos.");
                            }
                        }
                    });
                }
            }.runTaskTimer(getPlugin(), 1L, 10L);
        }

        private void stop() {
            stuck.clear();
            free.clear();
            unregister();
        }

        private void makeAnimation() {
            int RGM = 0, size = 8;
            double i;
            for (i = -2.0D; i < size; i += 0.9D) {
                RGM += 20;
                RegularColor regularColor = RegularColor.fromHSVHue(RGM);
                for (int d = 0; d <= 90; d++) {
                    Location particleLoc = new Location(center.getWorld(), center.getX() + Math.cos(d) * size, center.getY() + i, center.getZ() + Math.sin(d) * size);
                    new ParticleBuilder(ParticleEffect.SPELL_MOB, particleLoc).setParticleData(regularColor).displayNearby();
                }
            }
        }

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            if (stuck.contains(event.getPlayer())) {

                if (validate(event.getTo(), event.getFrom()))
                    return;

                event.setTo(event.getFrom());
            }
        }

        @EventHandler
        public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
            if ((event.getDamager() instanceof Player && event.getEntity() instanceof Player)) {
                Player player = (Player) event.getEntity();

                if (stuck.contains(player)) {
                    this.free.add(player);
                    this.stuck.remove(player);
                }
            }
        }

        @EventHandler
        public void onArrowDamage(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Arrow && event.getEntity() instanceof Player))
                return;

            final Player player = (Player) event.getEntity();

            if (stuck.contains(player)) {
                this.free.add(player);
                this.stuck.remove(player);
            }
        }

        protected boolean validate(Location first, Location second) {
            if (first == null || second == null)
                return false;
            if (!first.getWorld().equals(second.getWorld()))
                return false;
            if (first.getX() != second.getX())
                return false;
            if (first.getY() != second.getY())
                return false;
            return first.getZ() == second.getZ();
        }
    }
}
