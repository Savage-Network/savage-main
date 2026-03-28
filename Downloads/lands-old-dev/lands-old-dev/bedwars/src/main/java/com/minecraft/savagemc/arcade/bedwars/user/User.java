package com.minecraft.savagemc.arcade.bedwars.user;

import com.minecraft.savagemc.arcade.bedwars.arcade.Arcade;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.structure.team.Team;
import com.minecraft.savagemc.arcade.bedwars.user.context.UserContext;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.route.join.Join;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.bukkit.user.UserModel;
import com.minecraft.savagemc.core.member.list.bedwars.BedMember;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.BedOre;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.BedShop;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.stats.BedStats;
import com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil;
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

    private final BedMember member;
    private UserContext context;

    private Arena arena;
    private Team team;

    private int respawnTime;

    public User(BedMember member, Arena arena, Join join) {
        super(member.getId(), join);

        this.member = member;
        this.arena = arena;

        this.context = new UserContext();
    }

    @Override
    public void handle() {
        Player player = getAccount().player();

        setSidebar(new Sidebar(player, "§6§lBED WARS"));

        if (arena != null)
            arena.join(player);
    }

    public void reset() {
        setContext(new UserContext());
    }

    public Player getPlayer() {
        return getAccount().player();
    }

    public Arcade getArcade() {
        return arena.getArcade();
    }

    @Override
    public void setState(ArcadeState state) {
        super.setState(state);

        Player player = getPlayer();

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

                    this.respawnTime = 5;
                    break;
                }

                case DEAD: {
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

    public boolean isProtected() {
        if (!isPlayer() || !inState(ArcadeState.ALIVE)) return true;

        if (context.getProtectionTime() > -1L && context.getProtectionTime() > System.currentTimeMillis()) return true;

        return !arena.isPhase(RoomPhase.PLAYING);
    }

    public boolean isImmuneTraps() {
        return context.getMilkTime() > System.currentTimeMillis();
    }

    public int getXpGain() {
        return context.getXpGain();
    }

    public void setXpGain(int xpGain) {
        context.setXpGain(getXpGain() + xpGain);
    }

    /* Stats */
    public int getKills() {
        return context.getKills();
    }

    public int getDeaths() {
        return context.getDeaths();
    }

    public int getFinalKill() {
        return context.getFinalKill();
    }

    public int getBedDestruction() {
        return context.getBedDestruction();
    }

    public void setKills() {
        context.setKills(getKills() + 1);

        BedStats stats = member.getStats(getArcade().getCategory(), arena.getType());

        if (stats != null) {
            stats.setKills();

            member.updateStats(stats);
        }
    }

    public void setDeaths() {
        context.setDeaths(getDeaths() + 1);

        BedStats stats = member.getStats(getArcade().getCategory(), arena.getType());

        if (stats != null) {
            stats.setDeaths();

            member.updateStats(stats);
        }
    }

    public void setMatches() {
        BedStats stats = member.getStats(getArcade().getCategory(), arena.getType());

        if (stats != null) {
            stats.setMatches();

            member.updateStats(stats);
        }
    }

    public void setFinalKill() {
        context.setFinalKill(getFinalKill() + 1);

        BedStats stats = member.getStats(getArcade().getCategory(), arena.getType());

        if (stats != null) {
            stats.setFinalKill();

            member.updateStats(stats);
        }
    }

    public void setBedDestruction() {
        context.setBedDestruction(getBedDestruction() + 1);

        BedStats stats = member.getStats(getArcade().getCategory(), arena.getType());

        if (stats != null) {
            stats.setBedDestruction();

            member.updateStats(stats);
        }
    }

    /* Item Methods */
    public boolean canBuyItem(BedWarsItem item) {
        return BukkitUtil.getItemAmount(getPlayer(), item.getOre().getMaterial()) >= item.getPrice();
    }

    public int getRemainingOre(BedWarsItem item) {
        return item.getPrice() - BukkitUtil.getItemAmount(getPlayer(), item.getOre().getMaterial());
    }

    public void removeOre(BedWarsItem item) {
        BukkitUtil.removeItemByAmount(getPlayer(), item.getOre().getMaterial(), item.getPrice());
    }

    public int getRemainingOre(BedOre ore, int price) {
        return price - BukkitUtil.getItemAmount(getPlayer(), ore.getMaterial());
    }

    public void removeOre(BedOre ore, int price) {
        BukkitUtil.removeItemByAmount(getPlayer(), ore.getMaterial(), price);
    }

    public boolean hasTool(BedWarsItem item) {
        if (!item.isType(BedShop.TOOLS)) return false;

        BedWarsItem pickaxe = context.getPickaxeItem(),
                axe = context.getAxeItem();

        return item.isToolPickaxe() && item.isInferior(pickaxe) || item.isToolAxe() && item.isInferior(axe);
    }

    public boolean hasArmor(BedWarsItem item) {
        if (!item.isType(BedShop.ARMOR)) return false;

        BedWarsItem armor = context.getArmorItem();

        return armor != null && armor.isType(BedShop.ARMOR) && armor.ordinal() >= item.ordinal();
    }
}
