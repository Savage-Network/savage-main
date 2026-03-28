package com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.phase;

import com.minecraft.savagemc.arcade.bedwars.BedWars;
import com.minecraft.savagemc.arcade.bedwars.arcade.Arcade;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.event.Event;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.top.Top;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.top.user.TopUser;
import com.minecraft.savagemc.arcade.bedwars.structure.team.Team;
import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.room.map.area.Cuboid;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.room.slot.Slot;
import com.minecraft.savagemc.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.savagemc.core.arcade.room.type.Type;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.arcade.slime.SlimeWorldController;
import com.minecraft.savagemc.core.bukkit.api.title.TitleAnimation;
import com.minecraft.savagemc.core.bukkit.api.vanish.Vanish;
import com.minecraft.savagemc.core.bukkit.user.UserModel;
import com.minecraft.savagemc.core.member.context.elo.Elo;
import com.minecraft.savagemc.core.member.list.bedwars.BedMember;
import com.minecraft.savagemc.core.util.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.minecraft.savagemc.core.util.list.StringUtil.StringHelper.*;

public class ArenaPhaseExecutor {

    public static void execute(Arena arena, RoomPhase phase) {
        List<User> users = arena.getUsers()
                .stream().filter(user -> !Vanish.has(user.getPlayer()))
                .collect(Collectors.toList());

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

                arena.getContext().setEvent(new Event());
                arena.spawnGenerators();

                /* Definir times */
                for (Team team : arena.getTeamList()) {
                    if (team == null || team.isFull()) continue;

                    for (User user : users) {
                        if (user == null || !user.isPlayer()) continue;

                        Team userTeam = user.getTeam();

                        if (userTeam == null) {
                            Tag tag = Tag.of(team.getTypeName());

                            user.setTeam(team);
                            break;
                        }
                    }
                }

                /* Atualizar dados dos jogadores */
                users.forEach(user -> {
                    Player player = user.getPlayer();

                    Team team = user.getTeam();

                    Location base = team.getBase();

                    if (base != null)
                        player.teleport(base);

                    Tag tag = Tag.of(team.getTypeName());

                    if (tag != null) {
                        Savage.message("Tag: " + tag.getName());

                        user.setTag(tag);
                    }

                    user.setMatches();

                    arcade.load(user);

                    arena.handleEntities(user);

                    user.getAccount().setRoute(arena, team.getCodeId(), user.getState(), user.getJoin());
                });

                arena.getAllUsers().stream().filter(UserModel::isVanish).forEach(user -> {
                    Player player = user.getPlayer();

                    user.setState(ArcadeState.DEAD);

                    arcade.handleSidebar(user);

                    Savage.getPlatform().runSync(() -> arcade.handleHotbar(user), 5L);

                    arena.getPlayers().stream().filter(search -> !search.equals(player)).findFirst().ifPresent(player::teleport);
                });

                handleDestroyColiseum(arena);

                // Remover times mortos
                Savage.getPlatform().runSync(() -> {
                    for (Team team : arena.getTeamList()) {
                        if (team.getAliveUsers().isEmpty()) {
                            Location bed = team.getBedLocation();

                            if (bed != null)
                                bed.getBlock().setType(Material.AIR);
                        }
                    }

                    arena.getAllUsers().forEach(arena::updateSidebarTeams);
                }, 5L);

                // Tocar um trovão nas ilhas
                if (arena.isType(Type.COMPETITIVE))
                    arena.getTeamList().forEach(team -> {
                        if (!team.isDead()) {
                            Location base = team.getBase();

                            if (base != null)
                                base.getWorld().strikeLightningEffect(base);
                        }
                    });

                arena.send("",
                        "§4§lAVISO: §cAliar-se com outros jogadores é proibido e resulta em punição!",
                        "");
                arena.sound(Sound.LEVEL_UP);

                arena.getAllUsers().forEach(arena::hideAndShow);

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

                if (arena.hasWinner()) {
                    Team winner = (Team) arena.getWinner();

                    List<TeamPreset> losers = arena.getLosers().stream().map(team -> (TeamPreset) team).collect(Collectors.toList());

                    // Enviar títulos (Vitória / Derrota)
                    sendEndGameTitles(winner, losers, winner.getColor()
                            + (arena.isSlot(Slot.SOLO) ? winner.getPlayers().stream().map(HumanEntity::getName).findFirst().orElse(winner.getName()) : winner.getName())
                            + "§e venceu!");

                    // Criar mensagem de vitória
                    String winnerName = winner.getColor()
                            + (arena.isSlot(Slot.SOLO) ? winner.getPlayers().stream().map(HumanEntity::getName).findFirst().orElse(winner.getName()) : winner.getName());

                    Savage.getPlatform().runSync(() -> {
                        for (Player winnerPlayer : winner.getPlayers()) {
                            if (winnerPlayer == null) continue;

                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                if (onlinePlayer == winnerPlayer || winner.getPlayers().stream().noneMatch(search -> search.getUniqueId().equals(onlinePlayer.getUniqueId())))
                                    continue;

                                System.out.println("Escondeu " + onlinePlayer.getName() + " de " + winnerPlayer.getName());

                                winnerPlayer.hidePlayer(onlinePlayer);
                            }
                        }
                    });

                    arena.send(winnerName + "§e venceu a partida.");
                }

                arena.sound(Sound.ENDERDRAGON_GROWL, 13, 1);
                break;
            }
            case RESETTING: {
                users.forEach(user -> {
                    Player player = user.getPlayer();

                    if (player != null)
                        player.performCommand("playagain");
                });

                SlimeWorldController.unloadWorld(arena.getWorld(), () -> arcade.removeArena(arena));

                Savage.getPlatform().runSync(() -> {
                    SlimeWorldController.unloadWorld(arena.getWorld(), () -> {
                        arcade.removeArena(arena);
                        arcade.copyArena(arena);
                    });
                }, 30);

                break;
            }
        }
    }

    private static void sendEndGameTitles(TeamPreset winner, List<TeamPreset> losers, String subTitle) {
        winner.getPlayers().forEach(player -> TitleAnimation.sendWinFrame(player, subTitle));
        losers.forEach(loser -> loser.getPlayers().forEach(player -> TitleAnimation.sendDefeatFrame(player, subTitle)));
    }

    protected static void handleDestroyColiseum(Arena arena) {
        Location spawn = arena.getLocation("spawn");

        Instant now = Instant.now();

        Objects.requireNonNull(spawn, "O spawn da arena \"" + arena.getIdentifier() + "\" não foi encontrado.");

        // Dividir em partes para processar em chunks
        List<Block> blockList = Cuboid.getBlocksFromCenter(spawn, 25, 30, 5)
                .stream()
                .filter(block -> block != null && !block.isEmpty()).distinct().collect(Collectors.toList());

        int chunkSize = 100; // Número de blocos processados por execução

        AtomicInteger currentIndex = new AtomicInteger(0);

        // Início da tarefa escalonada
        new BukkitRunnable() {
            @Override
            public void run() {
                int startIndex = currentIndex.getAndAdd(chunkSize);
                if (startIndex >= blockList.size()) {
                    cancel(); // Cancelar a tarefa quando terminar
                    Savage.scan("Coliseu/" + arena.getIdentifier(), now);
                    return;
                }

                int endIndex = Math.min(startIndex + chunkSize, blockList.size());
                for (int i = startIndex; i < endIndex; i++) {
                    Block block = blockList.get(i);

                    // Remover entidades próximas ao bloco
                    block.getWorld().getNearbyEntities(block.getLocation(), 2, 0, 2)
                            .stream()
                            .filter(entity -> entity instanceof Item)
                            .forEach(org.bukkit.entity.Entity::remove);

                    // Remover o bloco
                    block.setType(Material.AIR);
                    block.getState().update(true);
                }
            }
        }.runTaskTimer(BedWars.getInstance(), 0, 1);
    }

    protected static void handleEndMessage(User user, Arena arena) {
        String separatorBar = "§a--------------------------------------";

        Team winner = (Team) arena.getWinner();

        if (winner == null) return;

        ArcadeCategory arcade = arena.getArcade().getCategory();

        List<String> messageList = new ArrayList<>(Arrays.asList(
                "",
                separatorBar,
                makeCenteredMessage((arena.isType(Type.CASUAL) ? "§6" : "§5") + "§l "
                        + arcade.getServer().getName().toUpperCase() + " " + arcade.getName().toUpperCase()),
                makeCenteredMessage("§eVencedor: " + winner.getColor() + "Time " + winner.getName()),
                ""
        ));

        // Renderizar tops
        for (Top top : arena.getContext().getTops()) {
            if (top == null || top.getUsers().isEmpty()) continue;

            messageList.add(makeCenteredMessage("§b§lTOP " + top.getType().getName().toUpperCase()));

            List<TopUser> topList = top.getTopThird();

            for (TopUser topUser : topList) {
                Account account = topUser.getAccount();

                if (account == null) continue;

                int index = topList.indexOf(topUser) + 1;

                String tagPrefix = account.getTag().getColor()
                        + (account.getTag().ordinal() > Tag.MIDIA_PLUS.ordinal() ? "§o" : "");

                String color = index == 1 ? "§a" : index == 2 ? "§e" : "§c";

                // 1º CassioMartim - 5
                messageList.add(makeCenteredMessage(
                        color + index + "º " + tagPrefix + account.getNickname() + " §7- " + Util.formatNumber(topUser.getValue())
                ));
            }

            messageList.add("");
        }
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
        BedMember member = user.getMember();

        if (arena.inTop(user)) {
            int xpForBeingAtTop = Savage.RANDOM.ints(120, 250).findFirst().orElse(120);

            if (arena.isType(Type.CASUAL))
                member.addLevelXp(xpForBeingAtTop);
            else
                member.addEloXp(xpForBeingAtTop);

            user.setXpGain(xpForBeingAtTop);

            account.send("§b+" + xpForBeingAtTop + " XP");
        }

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
