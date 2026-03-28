package com.minecraft.game.arcade.arena.phase;

import com.minecraft.game.arcade.arena.Arena;
import com.minecraft.game.user.User;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.bukkit.api.title.TitleAnimation;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class ArenaPhaseExecutor {

    public static void execute(Arena arena, RoomPhase phase) {
        switch (phase) {
            case WAITING: {
                arena.setTime(30);
                break;
            }

            case STARTING: {
                arena.setTime(30);
                break;
            }

            case PLAYING: {
                arena.send("", "§a§lJOGO INICIADO!", "");

                arena.getUsers().forEach(user -> {
                    Player player = user.getAccount().player();

                    user.setState(ArcadeState.ALIVE);

                    arena.getArcade().handleSidebar(user);
                    arena.getArcade().buildHotbar(player);

                    TitleAnimation.sendVictoryFrame(player, "§a§lCOMEÇOU!", TimeUnit.SECONDS.toMillis(2));
                });

                break;
            }

            case ENDING: {
                arena.setTime(10);

                User winner = arena.getAliveUsers().stream().findFirst().orElse(null);

                if (winner != null) {
                    Account account = winner.getAccount();

                    arena.send("", "§6§lVITÓRIA! §e" + account.getNickname() + " venceu!", "");

                    TitleAnimation.sendVictoryFrame(account.player(), "§6§lVITÓRIA!", TimeUnit.SECONDS.toMillis(5));

                    arena.getUsers().stream()
                            .filter(user -> !user.equals(winner))
                            .forEach(user -> TitleAnimation.sendDefeatFrame(user.getAccount().player(), "§c§lDERROTA!", TimeUnit.SECONDS.toMillis(5)));
                } else {
                    arena.send("", "§c§lEMPATE!", "");
                }

                arena.sound(Sound.LEVEL_UP);

                break;
            }

            case RESETTING: {
                arena.getUsers().forEach(user -> {
                    Account account = user.getAccount();

                    account.redirect(Savage.getServerData().local().getType());
                });

                break;
            }
        }
    }
}
