package com.minecraft.game.arcade;

import com.grinderwolf.swm.plugin.SWMPlugin;
import com.minecraft.game.Game;
import com.minecraft.game.arcade.arena.Arena;
import com.minecraft.game.user.User;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.arcade.ArcadeHolder;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.room.map.Map;
import com.minecraft.savagemc.core.arcade.room.slot.Slot;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.arcade.slime.SlimeWorldController;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.event.IgnoreEvent;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.core.util.list.DateUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@IgnoreEvent
public abstract class Arcade extends ArcadeHolder {

    private final File mapsDirectory;

    public Arcade(String mapsDirectory, Integer minRooms, Integer maxRooms, ArcadeCategory category) {
        super(mapsDirectory, minRooms, maxRooms, category);

        this.mapsDirectory = new File(Game.getInstance().getDataFolder().getAbsolutePath() + "/maps");
    }

    public abstract void handleSidebar(User user);

    @Override
    public boolean load() {
        Savage.Savage.Logger().info("Iniciando " + getName() + "...");

        if (!loadMaps(mapsDirectory)) return false;

        List<Map> availableMaps = getMaps();

        if (availableMaps.isEmpty()) {
            Savage.Savage.Logger().warning("Nenhum mapa disponível para " + getName() + ".");
            return false;
        }

        int numRooms = getMinRooms();
        int mapIndex = 0;

        for (int i = 0; i < numRooms; i++) {
            Map map = availableMaps.get(mapIndex);

            if (setupRoom(map, getCategory().getSlots().get(0)) == null) return false;

            mapIndex = (mapIndex + 1) % availableMaps.size();
        }

        Savage.Savage.Logger().info("Modo de jogo " + getName() + " iniciado com sucesso.");
        return true;
    }

    @Override
    public void unload() {
        Savage.Savage.Logger().info("Encerrando jogo " + getName() + "...");
        getRooms().clear();
        Savage.Savage.Logger().info("O jogo " + getName() + " foi desligado com sucesso!");
    }

    @Override
    public Arena setupRoom(Map map, Slot slot) {
        Instant now = Instant.now();

        int id = (getId().getAndIncrement() + 1);

        Arena arena = new Arena(id, this, map, slot);

        Savage.Savage.Logger().info("[" + getName() + "/" + id + "] Criando arena...");

        String templateName = getName().toLowerCase(),
                worldName = templateName + "-" + id;

        SlimeWorldController.cloneWorldFromTemplate(SWMPlugin.getInstance(), templateName, worldName, () -> {
            arena.setMinPlayers(2);
            arena.setMaxPlayers(slot.getMaxPlayers());

            arena.handleBorder();

            getRooms().add(arena);

            Savage.Savage.Logger().info("[" + getName() + "/" + id + "] Arena criada com sucesso. (Tempo médio: " + Util.formatInstant(now) + ")");
        });

        return arena;
    }

    @Override
    public Arena findBestArena(UUID sender, ArcadeRouteContext route) {
        return (Arena) getRooms().stream()
                .filter(room -> room.isAvailable())
                .findFirst()
                .orElse(null);
    }

    public void load(User user) {
        handleSidebar(user);
        buildHotbar(user.getAccount().player());
    }

    public abstract void buildHotbar(Player player);

    public void updateSidebar(User user) {
        Sidebar sidebar = user.getSidebar();

        sidebar.updateRow("kills", "Kills: §a" + user.getKills());
        sidebar.updateRow("deaths", "Deaths: §c" + user.getDeaths());
    }
}
