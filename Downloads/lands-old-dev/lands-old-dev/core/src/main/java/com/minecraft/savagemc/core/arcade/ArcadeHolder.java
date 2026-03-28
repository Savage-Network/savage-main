package com.minecraft.savagemc.core.arcade;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.feature.ArcadeFeature;
import com.minecraft.savagemc.core.arcade.room.Room;
import com.minecraft.savagemc.core.arcade.room.map.Map;
import com.minecraft.savagemc.core.arcade.room.map.area.Cuboid;
import com.minecraft.savagemc.core.arcade.room.map.location.SignedLocation;
import com.minecraft.savagemc.core.arcade.room.map.location.SyntheticLocation;
import com.minecraft.savagemc.core.arcade.room.slot.Slot;
import com.minecraft.savagemc.core.arcade.room.type.Type;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.event.IgnoreEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static com.minecraft.savagemc.core.util.list.bukkit.MapUtil.getSignedLocation;

@Getter
@RequiredArgsConstructor
@IgnoreEvent
public abstract class ArcadeHolder implements Listener {

    private transient final AtomicInteger id = new AtomicInteger();

    private final String name = getClass().getSimpleName();

    private final String mapsDirectory;
    private final Integer minRooms, maxRooms;

    private final ArcadeCategory category;

    private final Set<Room> rooms = new HashSet<>();
    private final List<Map> maps = new ArrayList<>();

    public abstract boolean load();

    public abstract void unload();

    public abstract Room setupRoom(Map map, Slot slot);

    /**
     * @param sender Jogador remetente
     * @param route  Rota para busca
     * @return A melhor arena baseada na rota
     */
    public Room findBestArena(UUID sender, ArcadeRouteContext route) {
        return null;
    }

    public Room setupRoom(Map map, Slot slot, Type type) {
        return null;
    }

    @Override
    public boolean equals(Object arcadeObj) {
        if (arcadeObj == null || arcadeObj.getClass() != getClass()) return false;

        ArcadeHolder arcade = (ArcadeHolder) arcadeObj;

        return arcade.getCategory().equals(category) && arcade.getName().equalsIgnoreCase(name);
    }

    public boolean isCategory(ArcadeCategory category) {
        return this.category.equals(category);
    }

    /* Map Loader */
    public boolean loadMaps(File mapsFile) {
        File mapDirectory = new File(mapsFile, getName().toLowerCase());

        if (!mapDirectory.isDirectory()) {
            Savage.Savage.Logger().warning("O arquivo " + mapDirectory.getName() + " não é uma pasta.");
            return false;
        }

        File[] maps = mapDirectory.listFiles();

        if (maps == null || maps.length == 0) {
            Savage.Savage.Logger().warning("Nenhum mapa de " + getName() + " foi encontrado.");
            return false;
        }

        for (int index = 0; index < maps.length; index++) {
            File mapFile = maps[index];

            Savage.Savage.Logger().info("[" + (index + 1) + "/" + maps.length + "] Iniciando mapa " + mapFile.getName() + "...");

            try {
                File config = new File(mapFile, "config.json");

                if (!config.exists()) {
                    Savage.Savage.Logger().info("[" + (index + 1) + "/" + maps.length + "] A configuração do mapa " + mapFile.getName() + " não foi encontrada!");
                    continue;
                }

                JsonObject json = Savage.PARSER.parse(new FileReader(config)).getAsJsonObject();

                String name = json.get("name").getAsString();
                int buildLimit = json.get("build_limit").getAsInt();

                Map map = new Map(index + 1, name, getCategory(), mapFile, json, buildLimit);

                JsonArray locations = json.get("locations").getAsJsonArray();

                for (JsonElement location : locations) {
                    SignedLocation signed = getSignedLocation(location);

                    if (signed != null)
                        map.getLocations().add(signed);
                }

                SignedLocation pos_1 = map.getLocation("map_limit_pos1"),
                        pos_2 = map.getLocation("map_limit_pos2");

                if (pos_1 == null || pos_2 == null) continue;

                map.setArea(new Cuboid(pos_1.getSynthetic(), pos_2.getSynthetic()));

                if (json.has("normal")) {
                    boolean normal = json.get("normal").getAsBoolean();

                    map.setNormal(normal);
                }

                getMaps().add(map);

                Savage.Savage.Logger().info("[" + map.getId() + "/" + maps.length + "] Mapa " + map.getName() + " iniciado.");
            } catch (Exception e) {
                Savage.Savage.Logger().log(Level.WARNING, "Não foi possível carregar o mapa...", e);
                return false;
            }
        }

        return true;
    }

    /* Method Configuration */
    public Map getMap(int mapId) {
        return maps.stream().filter(map -> map.getId() == mapId).findFirst().orElse(null);
    }

    public Map getRandomMap() {
        if (maps.isEmpty()) return null;

        int mapId = Savage.RANDOM.nextInt(maps.size());

        return maps.get(mapId);
    }

    public boolean hasFeature(ArcadeFeature... features) {
        return Arrays.stream(features).anyMatch(category::hasFeature);
    }

    public boolean isArena(World world) {
        return rooms.stream().anyMatch(room -> room.getWorld() != null && room.getWorld().equals(world) && room.getArcade().isCategory(getCategory()));
    }

    public Room getRoom(int roomId) {
        return rooms.stream().filter(room -> room.getId() == roomId).findFirst().orElse(null);
    }

    public Room getRoom(World world) {
        return rooms.stream().filter(room -> room.getWorld().equals(world) && room.getArcade().isCategory(getCategory())).findFirst().orElse(null);
    }

    public Room getRoom(ArcadeRouteContext route) {
        return rooms.stream()
                .filter(room -> room.getId() == route.getRoomId() && room.isSlot(route.getSlot()) && room.isFiltered(route.getMapId()))
                .findFirst()
                .orElse(null);
    }
}
