package com.minecraft.savagemc.arcade.aquatic.arcade.arena;

import com.minecraft.savagemc.arcade.aquatic.arcade.Arcade;
import com.minecraft.savagemc.arcade.aquatic.arcade.arena.phase.ArenaPhaseExecutor;
import com.minecraft.savagemc.arcade.aquatic.arcade.team.Team;
import com.minecraft.savagemc.arcade.aquatic.user.User;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.arcade.room.Room;
import com.minecraft.savagemc.core.arcade.room.map.Map;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.room.slot.Slot;
import com.minecraft.savagemc.core.arcade.room.team.preset.TeamPresetType;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.util.list.TimeUtil;
import com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class Arena extends Room {

    public Arena(int id, Arcade arcade, Map map, Slot slot) {
        super(id, arcade, map, slot);
    }

    @Override
    public void join(Player player) {
        User user = (User) User.of(player.getUniqueId());

        Arcade arcade = user.getArcade();

        Account account = user.getAccount();

        user.setTag(!user.isPlayer() ? Tag.SPECTATOR : account.getTag());

        getPlayers().add(player);

        if (player.getMaxHealth() > 20.0D)
            player.setMaxHealth(20.0D);

        player.refresh();

        Location spawn = getLocation("spawn");

        if (user.isPlayer()) {
            user.setState(ArcadeState.ALIVE);

            for (Team team : getTeamList()) {
                if (team.isFull()) continue;

                user.setTeam(team);

                team.getMembers().add(player.getUniqueId());
                break;
            }

            player.teleport(spawn);

            Tag tag = user.getTag();

            send(tag.getColor() + (tag.ordinal() >= Tag.MIDIA_PLUS.ordinal() ? "§o" : "")
                    + account.getNickname() + "§e entrou na sala. (§b" + getTotalPlayers() + "§e/§b" + getMaxPlayers() + "§e)");
        } else {
            handleVanish(player);

            player.teleport(spawn);
        }

        hideAndShow(user);

        arcade.load(user);

        account.title("");

        account.setRoute(this, user.isPlayer() ? user.getTeam().getCodeId() : "", user.getState(), user.getJoin());
    }

    @Override
    public void quit(Player player) {
        User user = (User) User.of(player.getUniqueId());

        if (user.isPlayer() && !user.inState(ArcadeState.DEAD)) {
            Team team = user.getTeam();

            team.getMembers().remove(player.getUniqueId());

            if (team.getRemainingLives() > 0 && (isSlot(Slot.SOLO) || team.getMembers().isEmpty()))
                team.setRemainingLives(0);

            if (isPhase(RoomPhase.PLAYING))
                send(team.getColor() + player.getName() + "§e desconectou.");
        }

        getPlayers().remove(player);
    }

    public void handlePulse() {
        if (isPhase(RoomPhase.WAITING) && canBeCount())
            setPhase(RoomPhase.STARTING);

        if (isPhase(RoomPhase.STARTING)) {
            // Pode contabilizar
            if (canBeCount()) {
                if (isFull() && getTime() > 10)
                    setTime(10);

                setTime(getTime() - 1);

                final int time = getTime();

                if (time == 10) {
                    title("§c§l" + getTime(), "§csegundos!");

                    sound(Sound.CLICK);
                }

                if (time <= 5 && time >= 1) {
                    send("§eA partida inicia em §c" + time + "§e segundo" + (time > 1 ? "s" : "") + "!");
                    title((time <= 3 ? "§a" : "§c") + time, "§ePrepare-se!");

                    sound(Sound.CLICK);
                }

                if (time <= 0)
                    setPhase(RoomPhase.PLAYING);
            } else
                setPhase(RoomPhase.WAITING);
        }

        if (isPhase(RoomPhase.PLAYING)) {
            setFullTime(getFullTime() + 1);

            if (getTotalPlayers() >= 1) {
                /* Verificando estado dos jogadores */
                for (Player player : getPlayers()) {
                    User user = (User) User.of(player.getUniqueId());

                    if (user == null || !user.isPlayer() || !user.inState(ArcadeState.RESPAWN) || user.getContext().getRespawnTime() < 0)
                        continue;

                    Account account = user.getAccount();

                    Arcade arcade = user.getArena().getArcade();

                    int time = user.getContext().getRespawnTime();

                    user.getContext().setRespawnTime(time - 1);

                    if (time >= 1 && time <= 5) {
                        account.title("§c§lVOCÊ MORREU!", "§eRenascendo em §c" + time + "§e segundo" + (time > 1 ? "s" : "") + "!",
                                60, 100);

                        account.send("§c" + time + "...");
                        account.sound(Sound.WOOD_CLICK);
                    }

                    if (time == 0) {
                        final ArcadeState state = user.getState();

                        if (!state.equals(ArcadeState.ALIVE))
                            account.sound(Sound.LEVEL_UP);

                        Location spawn = user.getTeam().getBase();

                        if (spawn != null)
                            player.teleport(spawn);

                        user.setState(ArcadeState.ALIVE);

                        Savage.getPlatform().runSync(() -> {
                            if (user.inState(ArcadeState.ALIVE))
                                arcade.handleHotbar(user);
                        }, 2L);

                        hideAndShow(user);

                        account.title("", "§aRenasceu!");
                    }
                }
            } else {
                send("§4§lAVISO: §cNão há jogadores suficientes para continuar a partida!");

                Team winner = getTeamList().stream().filter(team -> !team.getPlayers().isEmpty()).findFirst().orElse(null);

                setWinner(winner);
                setPhase(RoomPhase.ENDING);
            }
        }

        if (isPhase(RoomPhase.ENDING)) {
            setTime(getTime() - 1);

            if (getTime() <= 0 || getPlayers().isEmpty())
                setPhase(RoomPhase.RESETTING);
            else
                getWinner().getPlayers().forEach(player -> Savage.getPlatform().runSync(() -> BukkitUtil.launchFirework(player.getLocation())));
        }

        getUsers().forEach(user -> updateSidebarTime(user.getSidebar()));
    }

    @Override
    public void setPhase(RoomPhase phase) {
        if (isPhase(phase)) return;

        super.setPhase(phase);
        ArenaPhaseExecutor.execute(this, phase);
    }

    public void hideAndShow(User user) {
        Player mainPlayer = user.getAccount().player();

        // 1º passo: Escondendo todos os jogadores
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target == null || target.equals(mainPlayer)) continue;

            target.hidePlayer(mainPlayer);
            mainPlayer.hidePlayer(target);
        }

        // 2º passo: Realizar filtragem e ver para quem mostrar o "mainPlayer"
        for (Player targetPlayer : getPlayers()) {
            if (targetPlayer == null || mainPlayer.equals(targetPlayer))
                continue;

            User target = (User) User.of(targetPlayer.getUniqueId());

            if (target == null || !target.getArena().equals(user.getArena())) continue;

            if (user.isVanish()) {
                mainPlayer.showPlayer(targetPlayer);

                if (target.isVanish())
                    targetPlayer.showPlayer(mainPlayer);
            }

            if (user.isSpectator() && !target.isVanish()) {
                mainPlayer.showPlayer(targetPlayer);

                if (target.isSpectator())
                    targetPlayer.showPlayer(mainPlayer);
            }

            if (user.isPlayer()) {
                if (isPhase(RoomPhase.ENDING) && target.isPlayer()) {
                    mainPlayer.showPlayer(targetPlayer);
                    targetPlayer.showPlayer(mainPlayer);
                }

                if (target.isPlayer() && !target.inState(ArcadeState.ALIVE) && !user.inState(ArcadeState.ALIVE))
                    mainPlayer.showPlayer(targetPlayer);

                if (target.isPlayer() && target.inState(ArcadeState.ALIVE))
                    mainPlayer.showPlayer(targetPlayer);

                if (user.inState(ArcadeState.ALIVE))
                    targetPlayer.showPlayer(mainPlayer);
            }
        }
    }

    public Arcade getArcade() {
        return (Arcade) super.getArcade();
    }

    @Override
    public boolean canBeCount() {
        return getTotalPlayers() >= getMinPlayers();
    }

    @Override
    public int getTotalPlayers() {
        return getUsers().size();
    }

    public boolean isFinalKill(User user) {
        return user.getTeam().isDead();
    }

    public void handleSidebarTeams(User user) {
        Sidebar sidebar = user.getSidebar();

        Team userTeam = user.getTeam();

        for (Team team : getTeamList()) {
            if (team == null || team.isDead()) continue;

            boolean hasFirstPlayer = team.getFirstMember() != null,
                    itsYou = (user.isPlayer() && team.equals(userTeam) && !user.inState(ArcadeState.DEAD));

            if (isSlot(Slot.SOLO) && hasFirstPlayer)
                sidebar.addRow(team.getCodeId(), team.getColor() + team.getFirstMember().getName() + ": " + team.getAliveDisplay()
                        + (itsYou ? " §7(VOCÊ)" : ""));
            else
                sidebar.addRow(team.getCodeId(), team.getDisplay() + ": " + team.getAliveDisplay() + (itsYou ? " §7(VOCÊ)" : ""));
        }
    }

    public void updateSidebarTeams(User user) {
        Sidebar sidebar = user.getSidebar();

        Team userTeam = user.getTeam();

        for (Team team : getTeamList()) {
            if (team == null || team.isDead()) continue;

            boolean hasFirstPlayer = team.getFirstMember() != null,
                    itsYou = (user.isPlayer() && team.equals(userTeam) && !user.inState(ArcadeState.DEAD));

            if (isSlot(Slot.SOLO) && hasFirstPlayer)
                sidebar.updateRow(team.getCodeId(), team.getColor() + team.getFirstMember().getName() + ": " + team.getAliveDisplay()
                        + (itsYou ? " §7(VOCÊ)" : ""));
            else
                sidebar.updateRow(team.getCodeId(), team.getDisplay() + ": " + team.getAliveDisplay() + (itsYou ? " §7(VOCÊ)" : ""));
        }
    }

    public void handleTimeSidebar(Sidebar sidebar) {
        String prefix = isPhase(RoomPhase.STARTING)
                ? "Inicia em §a"
                : isPhase(RoomPhase.ENDING)
                ? "§cFim de jogo!"
                : "Aguardando...";

        String suffix = isPhase(RoomPhase.STARTING)
                ? TimeUtil.formatTime(getTime())
                : "";

        sidebar.addRow("time", prefix + suffix);
        sidebar.blankRow();
    }

    public void updateSidebarTime(Sidebar sidebar) {
        String prefix = isPhase(RoomPhase.STARTING)
                ? "Inicia em §a"
                : isPhase(RoomPhase.ENDING)
                ? "§cFim de jogo!"
                : "Aguardando...";

        String suffix = isPhase(RoomPhase.STARTING)
                ? TimeUtil.formatTime(getTime())
                : "";

        sidebar.updateRow("time", prefix + suffix);
        sidebar.updateRow("players", "Players: §a" + getTotalPlayers() + "/" + getMaxPlayers());
    }

    public List<User> getUsers() {
        return User.list(user -> isPlayer(user.getAccount().getId())).stream().map(user -> (User) user).collect(Collectors.toList());
    }

    public List<User> getAliveUsers() {
        return getUsers().stream().filter(user -> user.isPlayer() && user.inState(ArcadeState.ALIVE)).collect(Collectors.toList());
    }

    public List<Team> getTeamList() {
        return getTeams().stream().map(team -> (Team) team).collect(Collectors.toList());
    }

    public List<User> getAliveUsers(Team team) {
        return getAliveUsers().stream().filter(user -> user.getTeam().equals(team)).collect(Collectors.toList());
    }

    @Override
    public Team buildTeam(TeamPresetType preset) {
        Location location = getLocation(preset.name().toLowerCase() + "_spawn");

        if (location == null)
            location = getLocation(preset.getName().toLowerCase() + "_spawn");

        return new Team(preset, this, location, getSlot().getMaxPlayers() / 2);
    }

    public List<Team> buildTeams() {
        return Arrays.stream(TeamPresetType.values())
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .map(this::buildTeam)
                .collect(Collectors.toList());
    }

    public List<Team> buildTeams(ChatColor... colors) {
        List<Team> teamList = buildTeams();

        if (colors == null || colors.length == 0)
            return teamList;

        Set<ChatColor> colorSet = new HashSet<>(Arrays.asList(colors));

        return teamList.stream()
                .filter(team -> colorSet.contains(team.getColor()))
                .collect(Collectors.toList());
    }

    public Team getTeam(Location bedLocation) {
        return getTeamList().stream().filter(team -> team.isYourBed(bedLocation)).findFirst().orElse(null);
    }

    public List<Team> getAliveTeams() {
        return getTeamList().stream().filter(team -> !team.isDead()).collect(Collectors.toList());
    }
}
