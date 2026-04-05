package com.minecraft.savagemc.arcade.duels.arcade.arena.phase;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.api.viewer.MenuViewer;
import com.minecraft.savagemc.core.arcade.feature.ArcadeFeature;
import com.minecraft.savagemc.core.arcade.room.cabin.Cabin;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.room.slot.Slot;
import com.minecraft.savagemc.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.savagemc.core.arcade.slime.SlimeWorldController;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.title.TitleAnimation;
import com.minecraft.savagemc.core.member.context.stats.ArcadeStats;
import com.minecraft.savagemc.core.member.list.duels.DuelMember;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.core.util.list.serialization.Serialization;
import com.minecraft.savagemc.arcade.duels.Duels;
import com.minecraft.savagemc.arcade.duels.arcade.Arcade;
import com.minecraft.savagemc.arcade.duels.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.duels.user.User;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PhaseAction {

    public static void handle(Arena arena, RoomPhase phase) {
        Set<Player> players = arena.getPlayers();

        Arcade arcade = arena.getArcade();

        switch (phase) {
            case WAITING: {
                players.forEach(player -> {
                    User user = (User) User.of(player.getUniqueId());

                    if (user != null) {
                        arcade.handleSidebar(user);
                        arcade.handleHotbar(player, arena);
                    }
                });
                break;
            }

            case STARTING: {
                arena.setTime(5);

                players.forEach(player -> {
                    User user = (User) User.of(player.getUniqueId());

                    if (user == null || !user.isPlayer()) return;

                    Location spawn = user.getTeam().getBase();

                    if (arcade.hasFeature(ArcadeFeature.WITHOUT_MOVING_STARTING)) {
                        player.clearInventory();
                        player.teleport(spawn);

                        user.setTag(Tag.of(user.getTeam().getColor().name()));

                        arena.setSkinChangeByPhase(player);
                        BukkitCore.getManager().getHologram().removeClients(player);
                    }

                    if (arcade.hasFeature(ArcadeFeature.CABINS)) {
                        Savage.getPlatform().runSync(() -> {
                            Cabin cabin = arena.createCabin(spawn, DyeColor.getByColor(user.getTeam().getRgb()));

                            if (cabin != null)
                                player.teleport(cabin.getSpawn());
                        });
                    }

                    arcade.handleSidebar(user);
                });
                break;
            }

            case RESTARTING: {
                players.forEach(player -> {
                    if (!arcade.isValid(player)) return;

                    User user = (User) User.of(player.getUniqueId());

                    BukkitCore.getManager().getCooldown().resetCooldown(player);

                    player.refresh();

                    Location spawn = user.getTeam().getBase();

                    if (arcade.hasFeature(ArcadeFeature.CABINS)) {
                        Savage.getPlatform().runSync(() -> {
                            Cabin cabin = arena.createCabin(spawn, DyeColor.getByColor(user.getTeam().getRgb()));

                            if (cabin != null)
                                player.teleport(cabin.getSpawn());
                        });
                    } else
                        player.teleport(spawn);

                    arcade.updateTeamStyle(arena, user.getSidebar());
                });

                arena.setTime(4);
                arena.setSuperPhase(RoomPhase.STARTING);
                break;
            }

            case PLAYING: {
                players.forEach(player -> {
                    User user = (User) User.of(player.getUniqueId());

                    if (user == null) return;

                    Account account = user.getAccount();

                    if (arcade.hasFeature(ArcadeFeature.CABINS))
                        arena.resetCabins(Duels.getPlugin(Duels.class));

                    if (user.isPlayer()) {
                        TeamPreset team = user.getTeam();

                        DuelMember member = user.getMember();

                        ArcadeStats stats = member.getStats(arcade.getCategory());

                        if (!arcade.hasFeature(ArcadeFeature.WITHOUT_MOVING_STARTING)) {
                            Tag tag = Tag.of(team.getColor().name());

                            user.setTag(tag);
                        }

                        Location spawn = team.getBase();

                        if (!arcade.hasFeature(ArcadeFeature.NOT_TELEPORT_STARTING))
                            player.teleport(spawn);

                        arcade.handleHotbar(player, arena);

                        Savage.getPlatform().runSync(() -> player.setWalkSpeed(0.2f));

                        if (!arcade.hasFeature(ArcadeFeature.WITHOUT_MOVING_STARTING))
                            account.title("");

                        if (!arcade.hasFeature(ArcadeFeature.WITHOUT_MOVING_STARTING)) {
                            arena.setSkinChangeByPhase(player);
                            BukkitCore.getManager().getHologram().removeClients(player);
                        }

                        arcade.start(player);

                        stats.setMatches();
                        member.updateStats(stats);
                    }

                    Savage.getPlatform().runSync(() -> {
                        arena.hideAndShow(user);
                        arcade.handleSidebar(user);
                    }, 2L);
                });
                break;
            }

            case ENDING: {
                arena.setTime(10);

                if (arena.hasWinner()) {
                    TeamPreset winner = (TeamPreset) arena.getWinner();

                    List<TeamPreset> losers = arena.getLosers().stream().map(team -> (TeamPreset) team).collect(Collectors.toList());

                    // Enviar títulos (Vitória / Derrota)
                    sendEndGameTitles(winner, losers, winner.getColor()
                            + (arena.isSlot(Slot.SOLO) ? winner.getPlayers().stream().map(HumanEntity::getName).findFirst().orElse(winner.getName()) : winner.getName())
                            + "§e venceu!");

                    // Atualizando estatísticas
                    arcade.updateStatisticalData(winner, losers);

                    // Criar mensagem de vitória
                    String winnerName = winner.getColor()
                            + (arena.isSlot(Slot.SOLO) ? winner.getPlayers().stream().map(HumanEntity::getName).findFirst().orElse(winner.getName()) : winner.getName());

                    arena.send(winnerName + "§e venceu a partida.");
                }

                /* Visualizar inventário da batalha */
                if (arena.isSlot(Slot.SOLO) && arena.hasWinner()) {
                    /* Recolhendo inventários */
                    players.forEach(player -> {
                        User user = (User) User.of(player.getUniqueId());

                        if (user != null)
                            user.setInventoryBase64(Serialization.serializeInventoryOfPlayer(player));
                    });

                    User winner = (User) User.of(arena.getWinner().getMembers().stream().findFirst().orElse(null)),
                            loser = (User) User.of(arena.getLoser().getMembers().stream().findFirst().orElse(null));

                    if (winner == null || loser == null) {
                        Savage.getLogger().log(Level.WARNING, "Não foi possível enviar os inventários de batalha.");
                    } else {
                        MenuViewer winnerMenu = new MenuViewer(winner.getAccount().getNickname(), winner.getInventoryBase64(), arena.getModeName(), winner.getAccount().player().getHealth()),
                                loserMenu = new MenuViewer(loser.getAccount().getNickname(), loser.getInventoryBase64(), arena.getModeName(), loser.getAccount().player().getHealth());

                        TextComponent message = new TextComponent("§aInventários de batalha (Clique para ver) ");

                        TextComponent winnerButton = new TextComponent(arena.getWinner().getColor() + winner.getAccount().getNickname());
                        winnerButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mvw " + winnerMenu.getId()));

                        TextComponent loserButton = new TextComponent(arena.getLoser().getColor() + loser.getAccount().getNickname());
                        loserButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mvw " + loserMenu.getId()));

                        message.addExtra(winnerButton);
                        message.addExtra(" §7/ ");
                        message.addExtra(loserButton);

                        arena.send(message);
                    }
                }

                // Ajeitando os jogadores
                players.forEach(player -> {
                    User user = (User) User.of(player.getUniqueId());

                    if (user.isPlayer()) {
                        player.clearPotionEffects();

                        user.setTag(Tag.SPECTATOR);
                        arcade.handleHotbar(player, arena);
                    }

                    arena.hideAndShow(user);
                    arcade.handleSidebar(user);

                    player.setExp(0);
                    player.setLevel(0);

                    if (arcade.hasFeature(ArcadeFeature.CABINS))
                        player.teleport(arena.getWinner().getBase());

                    BukkitCore.getManager().getCooldown().resetCooldown(player);
                });

                break;
            }

            case RESETTING: {
                players.forEach(player -> {
                    Account account = Savage.getAccountController().of(player.getUniqueId());

                    if (account != null)
                        account.redirectToHub();
                });

                Savage.getPlatform().runSync(() -> {
                    if (arena.getPlayers().isEmpty()) {
                        /* Partida com líquidos permitidos, é necessário realizar uma cópia nova da arena. */
                        if (arcade.hasFeature(ArcadeFeature.LIQUID)) {
                            SlimeWorldController.unloadWorld(arena.getWorld(), () -> {
                                arcade.unloadArena(arena);
                                arcade.copySourceArena(arena);
                            });
                        } else {
                            Instant now = Instant.now();

                            Savage.getLogger().info("[" + arcade.getName() + "/" + arena.getIdentifier() + "] Redefinindo...");

                            if (arcade.hasFeature(ArcadeFeature.BUILD)) {
                                try {
                                    arena.getRollbackBlocks().forEach(rollbackBlock -> {
                                        if (rollbackBlock != null && rollbackBlock.getBlock() != null)
                                            rollbackBlock.getBlock().setType(Material.AIR);
                                    });
                                    arena.getRollbackBlocks().clear();
                                } catch (Exception e) {
                                    Savage.getLogger().log(Level.WARNING, "Não foi possível realizar o rollback...", e);
                                }
                            }

                            if (arena.hasCabins())
                                arena.resetCabins(Duels.getPlugin(Duels.class));

                            arena.getTeams().forEach(team -> ((TeamPreset) team).setScore(0));

                            arena.setFullTime(0);
                            arena.setTime(0);

                            arena.setPhase(RoomPhase.WAITING);

                            Savage.getLogger().info("[" + arcade.getName() + "/" + arena.getIdentifier() + "/" + Util.formatInstant(now) + "] Arena redefinida com sucesso.");
                        }
                    }
                }, 30);

                break;
            }
        }
    }

    private static void sendEndGameTitles(TeamPreset winner, List<TeamPreset> losers, String subTitle) {
        winner.getPlayers().forEach(player -> TitleAnimation.sendWinFrame(player, subTitle));
        losers.forEach(loser -> loser.getPlayers().forEach(player -> TitleAnimation.sendDefeatFrame(player, subTitle)));
    }
}
