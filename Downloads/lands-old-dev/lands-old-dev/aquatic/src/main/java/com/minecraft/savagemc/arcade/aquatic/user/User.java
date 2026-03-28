package com.minecraft.savagemc.arcade.aquatic.user;

import com.minecraft.savagemc.arcade.aquatic.arcade.Arcade;
import com.minecraft.savagemc.arcade.aquatic.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.aquatic.arcade.team.Team;
import com.minecraft.savagemc.arcade.aquatic.perk.Perk;
import com.minecraft.savagemc.arcade.aquatic.user.context.UserContext;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.route.join.Join;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.bukkit.user.UserModel;
import com.minecraft.savagemc.core.member.context.stats.ArcadeStats;
import com.minecraft.savagemc.core.member.list.aquatic.AquaticMember;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class User extends UserModel {

    private final AquaticMember member;

    private Arena arena;
    private Sidebar sidebar;

    private Team team;
    private Perk perk;

    private UserContext context;

    public User(AquaticMember member, Arena arena, Join join) {
        super(member.getId(), join);

        this.member = member;
        this.arena = arena;

        this.context = new UserContext();
    }

    @Override
    public void handle() {
        Player player = getAccount().player();

        this.sidebar = new Sidebar(player, "WATER FIGHT");

        if (arena != null)
            arena.join(player);
    }

    @Override
    public void setState(ArcadeState state) {
        super.setState(state);

        Player player = getAccount().player();

        Savage.getPlatform().runSync(() -> {
            switch (state) {
                case RESPAWN: {
                    player.clearInventory();
                    player.clearPotionEffects();

                    player.setHealth(player.getMaxHealth());

                    player.setFlying(true);

                    player.addPotionEffects(new PotionEffect(PotionEffectType.BLINDNESS, 45, 3),
                            new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 3));

                    player.playSound(Sound.BLAZE_DEATH);

                    context.setRespawnTime(5);
                    break;
                }

                case DEAD: {
                    player.clearInventory();
                    player.clearPotionEffects();

                    player.setHealth(player.getMaxHealth());
                    player.setFlying(true);

                    player.addPotionEffects(new PotionEffect(PotionEffectType.BLINDNESS, 45, 3),
                            new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 3));

                    player.playSound(Sound.BLAZE_DEATH);
                    break;
                }

                default: {
                    context.setProtectionTime(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(1) + TimeUnit.MILLISECONDS.toMillis(5));

                    player.clearPotionEffects();

                    player.setHealth(player.getMaxHealth());
                    player.setFlying(false);
                    break;
                }
            }
        });
    }

    public int getKills() {
        return context.getKills();
    }

    public int getDeaths() {
        return context.getDeaths();
    }

    public int getFinalKill() {
        return context.getFinalKill();
    }

    public void setKills() {
        context.setKills(getKills() + 1);

        ArcadeStats stats = member.getStats(getArcade().getCategory());

        if (stats != null) {
            stats.setKills();

            member.updateStats(stats);
        }
    }

    public void setDeaths() {
        context.setDeaths(getDeaths() + 1);

        ArcadeStats stats = member.getStats(getArcade().getCategory());

        if (stats != null) {
            stats.setDeaths(stats.getDeaths() + 1);

            member.updateStats(stats);
        }
    }

    public void setMatches() {
        ArcadeStats stats = member.getStats(getArcade().getCategory());

        if (stats != null) {
            stats.setMatches();

            member.updateStats(stats);
        }
    }

    public void setFinalKill() {
        context.setFinalKill(getFinalKill() + 1);
    }

    public int getXpGain() {
        return context.getXpGain();
    }

    public void setXpGain(int xpGain) {
        context.setXpGain(getXpGain() + xpGain);
    }

    public Arcade getArcade() {
        return arena.getArcade();
    }

    public boolean isProtected() {
        if (!isPlayer() || !inState(ArcadeState.ALIVE)) return true;

        return !arena.isPhase(RoomPhase.PLAYING);
    }

    public boolean hasPerk() {
        return perk != null;
    }

    public boolean isUsingPerk(String name) {
        return hasPerk() && perk.getName().equalsIgnoreCase(name);
    }
}
