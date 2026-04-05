package com.minecraft.savagemc.core.bukkit.api.hologram;

import com.minecraft.savagemc.core.bukkit.api.hologram.row.HologramRow;
import com.minecraft.savagemc.core.bukkit.api.hologram.row.animated.AnimatedRow;
import com.minecraft.savagemc.core.bukkit.api.hologram.row.temporary.RowTemporary;
import com.minecraft.savagemc.core.bukkit.api.hologram.touch.TouchHandler;
import com.minecraft.savagemc.core.util.list.TimeUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
@Setter
@ToString
public abstract class Hologram {

    protected int id;
    protected final String tag;

    private final List<HologramRow> rows = new ArrayList<>();
    private final Set<Player> viewers = new HashSet<>();

    private final Map<String, Object> properties = new HashMap<>();

    private final Set<Integer> entityIds = new HashSet<>();

    private RowTemporary temporary;

    private TouchHandler touch;
    protected Location location;

    private final long expiresAt;

    public Hologram(String tag, Location location) {
        this(tag, location, -1L);
    }

    public Hologram(String tag, Location location, long expiresAt) {
        this.tag = tag;
        this.location = location;

        this.expiresAt = expiresAt;
    }

    public abstract void setText(List<String> lines);

    public abstract void updateRows();

    public boolean isTemporary() {
        return expiresAt != -1L;
    }

    public boolean hasExpired() {
        return expiresAt <= System.currentTimeMillis();
    }

    public World getWorld() {
        return location.getWorld();
    }

    public void teleport(Location location) {
        setLocation(location);

        if (rows.isEmpty()) return;

        location = location.clone();

        for (HologramRow row : rows) {
            row.teleport(location);

            location = location.clone().subtract(0, 0.25, 0);
        }
    }

    // Métodos adicionais para compatibilidade
    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setTemporary(RowTemporary temporary) {
        this.temporary = temporary;
    }

    public String getId() {
        return String.valueOf(id);
    }

    public String getTag() {
        return tag;
    }

    public TouchHandler getTouch() {
        return touch;
    }

    public boolean hasTouch() {
        return touch != null;
    }

    public void space() {
        List<HologramRow> rows = getRows().subList(1, getRows().size());

        double offsetY = 0.1;
        for (HologramRow row : rows) {
            Location location = row.getLocation().clone();
            location.subtract(0, offsetY, 0);

            row.teleport(location);
            offsetY += 0.05;
        }
    }

    public boolean isEntityId(int entityId) {
        return this.entityIds.contains(entityId);
    }

    public void setText(int index, String text) {
        HologramRow row = rows.get(index);

        row.setText(text);
        viewers.forEach(row::update);
    }

    public void setSmall(boolean option) {
        rows.forEach(row -> row.small(option));
    }

    public void addAnimatedRow(ItemStack helmet) {
        HologramRow last = rows.get(rows.size() - 1);
        Location loc = last != null ? last.getLocation().clone().subtract(0, 0.25, 0) : location.clone().subtract(0, 0.25, 0);

        AnimatedRow animated = new AnimatedRow(this, loc, helmet);

        rows.add(animated);
        viewers.forEach(animated::spawn);
    }

    public void addTemporaryRow(String text) {
        HologramRow last = rows.get(rows.size() - 1);
        Location loc = last != null ? last.getLocation().clone().subtract(0, 0.25, 0) : location.clone().subtract(0, 0.25, 0);

        RowTemporary temporary = new RowTemporary(this, loc, text);

        setTemporary(temporary);

        rows.add(temporary);
        viewers.forEach(temporary::spawn);
    }

    public void updateTemporaryRow() {
        if (temporary == null) return;

        temporary.setText(String.format(temporary.getPreview(), TimeUtil.formatTime(expiresAt, TimeUtil.TimeFormat.SHORT)));

        viewers.forEach(temporary::update);
    }

    public HologramRow getLastRow() {
        return !rows.isEmpty() ? rows.get(rows.size() - 1) : null;
    }

    public void spawnTo(Player player) {
        if (!viewers.contains(player)) {
            rows.forEach(line -> line.spawn(player));

            viewers.add(player);
        }
    }

    public void despawnTo(Player player) {
        if (viewers.contains(player)) {
            rows.forEach(line -> line.despawn(player));

            viewers.remove(player);
        }
    }

    public void writeProperty(String key, Object value) {
        properties.put(key.toLowerCase(), value);
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key.toLowerCase());
    }

    public Object getProperty(String key) {
        return properties.get(key.toLowerCase());
    }

    public Object getProperty(String key, Object defaultValue) {
        return properties.computeIfAbsent(key.toLowerCase(), k -> defaultValue);
    }

    public int getInt(String key) {
        return (int) properties.get(key.toLowerCase());
    }

    public int getInt(String key, int defaultValue) {
        return (int) properties.computeIfAbsent(key.toLowerCase(), k -> defaultValue);
    }

    public void removeProperty(String key) {
        properties.remove(key.toLowerCase());
    }

    // Métodos adicionais para compatibilidade
    public Set<Player> getViewers() {
        return viewers;
    }

    public List<HologramRow> getRows() {
        return rows;
    }

    public Set<Integer> getEntityIds() {
        return entityIds;
    }
}
