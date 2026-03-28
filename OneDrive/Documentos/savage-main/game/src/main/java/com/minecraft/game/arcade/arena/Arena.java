package com.minecraft.game.arcade.arena;

import com.minecraft.game.arcade.Arcade;
import com.minecraft.game.arcade.arena.phase.ArenaPhaseExecutor;
import com.minecraft.game.user.User;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.arcade.room.Room;
import com.minecraft.savagemc.core.arcade.room.map.Map;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.room.slot.Slot;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.util.list.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
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
        account.setRoute(this, "", user.getState(), user.getJoin());
    }

    @Override
    public void quit(Player player) {
        User user = (User) User.of(player.getUniqueId());

        if (user.isPlayer() && !user.inState(ArcadeState.DEAD)) {
            if (isPhase(RoomPhase.PLAYING))
                send("§e" + player.getName() + " desconectou.");
        }

        getPlayers().remove(player);
    }

    public void handlePulse() {
        if (isPhase(RoomPhase.WAITING) && canBeCount())
            setPhase(RoomPhase.STARTING);

        if (isPhase(RoomPhase.STARTING)) {
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

            if (getTotalPlayers() < 2) {
                send("§4§lAVISO: §cNão há jogadores suficientes para continuar a partida!");
                setPhase(RoomPhase.ENDING);
            }
        }

        if (isPhase(RoomPhase.ENDING)) {
            setTime(getTime() - 1);

            if (getTime() <= 0 || getPlayers().isEmpty())
                setPhase(RoomPhase.RESETTING);
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

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target == null || target.equals(mainPlayer)) continue;

            target.hidePlayer(mainPlayer);
            mainPlayer.hidePlayer(target);
        }

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
        return User.list(user -> isPlayer(user.getAccount().getId())).stream()
                .map(user -> (User) user)
                .collect(Collectors.toList());
    }

    public List<User> getAliveUsers() {
        return getUsers().stream()
                .filter(user -> user.isPlayer() && user.inState(ArcadeState.ALIVE))
                .collect(Collectors.toList());
    }
}
