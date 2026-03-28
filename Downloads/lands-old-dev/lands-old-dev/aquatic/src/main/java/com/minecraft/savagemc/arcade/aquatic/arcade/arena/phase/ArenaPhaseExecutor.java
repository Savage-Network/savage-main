package com.minecraft.savagemc.arcade.aquatic.arcade.arena.phase;

import com.minecraft.savagemc.arcade.aquatic.arcade.Arcade;
import com.minecraft.savagemc.arcade.aquatic.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.aquatic.arcade.team.Team;
import com.minecraft.savagemc.arcade.aquatic.user.User;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.room.map.area.Cuboid;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.room.type.Type;
import com.minecraft.savagemc.core.arcade.slime.SlimeWorldController;
import com.minecraft.savagemc.core.bukkit.api.title.TitleAnimation;
import com.minecraft.savagemc.core.member.context.elo.Elo;
import com.minecraft.savagemc.core.member.list.aquatic.AquaticMember;
import com.minecraft.savagemc.core.util.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.minecraft.savagemc.core.util.list.StringUtil.StringHelper.makeCenteredMessage;

public class ArenaPhaseExecutor {

    public static void execute(Arena arena, RoomPhase phase) {
        List<User> users = arena.getUsers();

        Arcade arcade = arena.getArcade();

        switch (phase) {
            case WAITING: {
                arena.setTime(0);

                users.forEach(arcade::handleSidebar);
                break;
            }
            case STARTING: {
                arena.setTime(5);

                users.forEach(arcade::handleSidebar);
                break;
            }
            case PLAYING: {
                arena.setTime(0);
                arena.setFullTime(0);

                /* Atualizar dados dos jogadores */
                users.forEach(user -> {
                    Player player = user.getAccount().player();

                    Team team = user.getTeam();

                    Tag tag = Tag.of(team.getTypeName());

                    if (tag != null)
                        user.setTag(tag);

                    Location base = team.getBase();

                    if (base != null)
                        player.teleport(base);

                    user.setMatches();

                    Savage.getPlatform().runSync(() -> {
                        arcade.load(user);
                    });
                });

                arena.send("",
                        "§4§lAVISO: §cAliar-se com outros jogadores é proibido e resulta em punição!",
                        "");
                arena.sound(Sound.ITEM_PICKUP);

                Savage.Savage.Logger().info("[" + arcade.getName() + "/" + arena.getIdentifier() + "/" + arena.getMap().getName() + "]" +
                        " A partida foi iniciada com " + arena.getTotalPlayers() + " jogadores!");

                break;
            }
            case ENDING: {
                arena.setTime(15);

                arcade.updateStatisticalData((Team) arena.getWinner(),
                        arena.getLosers().stream().map(team -> (Team) team).collect(Collectors.toList()));

                users.forEach(user -> {
                    CompletableFuture.runAsync(() -> arcade.load(user));

                    handleEndMessage(user, arena);
                });

                arena.sound(Sound.ENDERDRAGON_GROWL, 13, 1);
                break;
            }
            case RESETTING: {
                users.forEach(user -> user.getAccount().redirectToHub());

                Savage.getPlatform().runSync(() -> {
                    if (arena.getPlayers().isEmpty()) {
                        SlimeWorldController.unloadWorld(arena.getWorld(), () -> {
                            arcade.unloadArena(arena);
                            arcade.copyArena(arena);
                        });
                    }
                }, 30);
                break;
            }
        }
    }

    protected static void handleEndMessage(User user, Arena arena) {
        String separatorBar = "§a--------------------------------------";

        Team winner = (Team) arena.getWinner();

        ArcadeCategory arcade = arena.getArcade().getCategory();

        List<String> messageList = new ArrayList<>(Arrays.asList(
                "",
                separatorBar,
                makeCenteredMessage((arena.isType(Type.CASUAL) ? "§6" : "§5") + "§l "
                        + arcade.getServer().getName().toUpperCase() + " " + arcade.getName().toUpperCase()),
                makeCenteredMessage("§eVencedor: " + winner.getColor() + "Time " + winner.getName()),
                ""
        ));

        messageList.addAll(Arrays.asList(separatorBar, ""));

        Account account = user.getAccount();

        account.send(messageList);

        /* Mensagens individuais */
        TextComponent playAgainText = new TextComponent("§eDeseja jogar novamente? ");

        TextComponent playAgainButton = new TextComponent("§b§lCLIQUE AQUI");
        playAgainButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/playagain"));

        playAgainText.addExtra(playAgainButton);
        playAgainText.addExtra("\n");

        account.send(playAgainText);

        winner.getPlayers().forEach(player -> TitleAnimation.sendWinFrame(player, ""));

        // Estatísticas gerais
        AquaticMember member = user.getMember();

        account.send("§eVocê reivindicou §b" + Util.formatNumber(user.getXpGain()) + " de XP§e nessa partida!");

        if (arena.isType(Type.CASUAL)) {
            String current = member.getLevelId(), next = member.getNextLevelId();

            final int xp = member.getLevelXp(), minXp = member.getMinLevelXp();

            String[] barAndPercentage = buildBarAndPercentage(xp, minXp);

            account.send(current + barAndPercentage[0] + next + barAndPercentage[1]);
        } else {
            Elo current = member.getElo(), next = member.getNextElo();

            if (next == null) return;

            final int xp = member.getEloXp(), minXp = next.getMinXp();

            String[] barAndPercentage = buildBarAndPercentage(xp, minXp);

            account.send(current.getFullName() + barAndPercentage[0] + next.getFullName() + barAndPercentage[1],
                    "");
        }
    }

    protected static String[] buildBarAndPercentage(int current, int max) {
        return new String[]{
                " §8[" + Util.createProgressBar(ChatColor.GREEN, ChatColor.GRAY, "|", current, max, 50) + "§8] ",
                " §7(" + Util.formatNumberWithLetter(current) + "/" + Util.formatNumberWithLetter(max) + ") "
                        + Util.percentage(current, max) + "%"
        };
    }
}
