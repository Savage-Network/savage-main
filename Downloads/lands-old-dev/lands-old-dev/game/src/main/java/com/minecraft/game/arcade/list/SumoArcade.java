package com.minecraft.game.arcade.list;

import com.minecraft.game.arcade.Arcade;
import com.minecraft.game.arcade.arena.Arena;
import com.minecraft.game.user.User;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.event.IgnoreEvent;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.core.util.list.DateUtil;
import com.minecraft.savagemc.core.util.list.TimeUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@IgnoreEvent
public class SumoArcade extends Arcade {

    public SumoArcade() {
        super("sumo", 1, 5, ArcadeCategory.EVENT_SUMO);
    }

    @Override
    public void handleSidebar(User user) {
        Arena arena = user.getArena();
        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle("§6§lSUMO");

        sidebar.addRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + arena.getIdentifier());
        sidebar.blankRow();

        if (arena.isPhase(RoomPhase.PLAYING)) {
            sidebar.addRow("kills", "Kills: §a" + user.getKills());
            sidebar.addRow("deaths", "Deaths: §c" + user.getDeaths());
        } else {
            sidebar.addRow("map", "Mapa: §a" + arena.getMap().getName());
            sidebar.addRow("mode", "Modo: §a" + arena.getArcade().getCategory().getName());

            sidebar.blankRow();
            arena.handleTimeSidebar(sidebar);

            sidebar.addRow("players", "Players: §a" + arena.getTotalPlayers() + "/" + arena.getMaxPlayers());
        }

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
    }



    public void handleDeath(User user, User killer) {
        Arena arena = user.getArena();

        user.addDeath();
        user.setState(ArcadeState.DEAD);

        if (killer != null) {
            killer.addKill();

            killer.getAccount().send("§a+1 Kill!");
            killer.getAccount().sound(Sound.SUCCESSFUL_HIT);

            updateSidebar(killer);
        }

        buildHotbar(user.getAccount().player());

        arena.send("§c" + user.getAccount().getNickname() + " §7foi eliminado" + (killer != null ? " por §a" + killer.getAccount().getNickname() : "") + "§7!");

        if (arena.getAliveUsers().size() == 1) {
            arena.setPhase(RoomPhase.ENDING);
        }

        arena.hideAndShow(user);
    }

    @Override
    public void buildHotbar(Player player) {
        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        player.clearInventory();

        if (user.inState(ArcadeState.DEAD)) {
            player.getInventory().setItem(7, Item.of(Material.PAPER, "§aJogar novamente")
                    .interact(event -> event.getPlayer().performCommand("evento")));
        }

        player.getInventory().setItem(8, Item.of(Material.ACACIA_DOOR_ITEM, "§cVoltar ao lobby")
                .interact(event -> event.getPlayer().performCommand("lobby")));
    }
}
