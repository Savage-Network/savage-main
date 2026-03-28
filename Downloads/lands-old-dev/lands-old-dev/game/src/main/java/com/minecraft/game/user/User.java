package com.minecraft.game.user;

import com.minecraft.game.arcade.Arcade;
import com.minecraft.game.arcade.arena.Arena;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.arcade.route.join.Join;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.bukkit.user.UserModel;
import com.minecraft.savagemc.core.member.Member;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@Getter
@Setter
public class User extends UserModel {

    private final Member member;
    private Arena arena;
    private Sidebar sidebar;

    private int kills;
    private int deaths;

    public User(Member member, Arena arena, Join join) {
        super(member.getId(), join);

        this.member = member;
        this.arena = arena;
    }

    @Override
    public void handle() {
        Player player = getAccount().player();

        this.sidebar = new Sidebar(player, "EVENTOS");

        if (arena != null)
            arena.join(player);
    }

    @Override
    public void setState(ArcadeState state) {
        super.setState(state);

        Player player = getAccount().player();

        Savage.getPlatform().runSync(() -> {
            switch (state) {
                case DEAD: {
                    player.clearInventory();
                    player.clearPotionEffects();

                    player.setHealth(player.getMaxHealth());
                    player.setFlying(true);

                    player.playSound(Sound.BLAZE_DEATH);
                    break;
                }

                default: {
                    player.clearPotionEffects();
                    player.setHealth(player.getMaxHealth());
                    player.setFlying(false);
                    break;
                }
            }
        });
    }

    public Arcade getArcade() {
        return arena.getArcade();
    }

    public void addKill() {
        this.kills++;
    }

    public void addDeath() {
        this.deaths++;
    }
}
