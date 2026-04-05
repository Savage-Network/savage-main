package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.listener.DynamicListener;
import com.minecraft.core.bukkit.util.particle.ParticleBuilder;
import com.minecraft.core.bukkit.util.particle.ParticleEffect;
import com.minecraft.core.bukkit.util.particle.data.color.RegularColor;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Toxic extends Kit {

    public Toxic(HungerGames hungerGames) {
        super(hungerGames);
        setKitCategory(KitCategory.COMBAT);
        setIcon(Pattern.of(Material.SLIME_BALL));
        setItems(new ItemFactory(Material.SLIME_BALL).setName("§aCésio-137").setDescription("§7Kit Toxic").getStack());
        setPrice(35000);
        setCooldown(30);
        setReleasedAt(1634871600000L);
    }

    @Variable(name = "hg.kit.toxic.poison_amplifier")
    @Setter
    public int poisonAmplifier = 4;

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
        new DangerZone(getUser(player.getUniqueId()), poisonAmplifier).start();
    }

    public static class DangerZone extends DynamicListener implements Assistance {

        private final User owner;
        private final int poisonLevel;
        private final Location center;

        public DangerZone(User owner, int poisonLevel) {
            this.owner = owner;
            this.poisonLevel = poisonLevel;
            this.center = owner.getPlayer().getLocation();
        }

        public void start() {
            register();

            new BukkitRunnable() {
                int time = 15;

                public void run() {

                    if (time-- < 0) {
                        cancel();
                        unregister();
                        return;
                    }

                    animation();
                }
            }.runTaskTimer(getPlugin(), 20, 20);
        }

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            Player player = event.getPlayer();

            if (player.getEntityId() == owner.getPlayer().getEntityId())
                return;

            if (event.getTo().distance(center) < 5 && !getKit("Viper").isUser(player)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, poisonLevel), true);
            }
        }

        private void animation() {
            int RGM, size = 5;
            double i;
            for (i = -1.7D; i < 2.5; i += 0.7D) {
                RGM = 100;
                RegularColor regularColor = RegularColor.fromHSVHue(RGM);
                for (int d = 0; d <= 90; d++) {
                    Location particleLoc = new Location(center.getWorld(), center.getX() + Math.cos(d) * size, center.getY() + i, center.getZ() + Math.sin(d) * size);
                    new ParticleBuilder(ParticleEffect.SPELL_MOB, particleLoc).setParticleData(regularColor).displayNearby();
                }
            }
        }
    }
}
