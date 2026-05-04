package com.minecraft.arcade.eventos.managers;

import com.minecraft.arcade.eventos.Eventos;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Medalhas ganhas nos eventos (persistência YAML em {@code player-medals.yml}).
 */
public final class EventMedalStore {

    public static final String MEDAL_VITORIA = "vitoria";
    public static final String MEDAL_TOP_KILLER = "top_killer";
    public static final String MEDAL_PARTICIPACAO = "participacao";

    private final Eventos plugin;
    private final File file;

    public EventMedalStore(Eventos plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "player-medals.yml");
    }

    /** Conjunto imutável das medalhas (ids em minúsculas). */
    public Set<String> getMedals(UUID uuid) {
        if (uuid == null || !file.exists()) return Collections.emptySet();
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        List<String> raw = yaml.getStringList(camPathLista(uuid));
        if (raw == null || raw.isEmpty()) return Collections.emptySet();
        return Collections.unmodifiableSet(raw.stream().filter(Objects::nonNull).map(String::trim)
                .filter(s -> !s.isEmpty()).map(String::toLowerCase).collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    /** Adiciona uma medal ID; {@code false} se já existia. */
    public boolean addMedal(UUID uuid, String medalId) {
        if (uuid == null || medalId == null || medalId.isEmpty()) return false;
        String id = medalId.trim().toLowerCase();
        Set<String> set = new LinkedHashSet<>(getMedals(uuid));
        if (!set.add(id)) return false;
        gravarLista(uuid, set);
        return true;
    }

    public void addMedalTodos(Collection<UUID> uuids, String medalId) {
        if (uuids == null || medalId == null || medalId.isEmpty()) return;
        for (UUID u : uuids) {
            if (u != null) addMedal(u, medalId);
        }
    }

    /** Força gravar no disco (ex.: onDisable). */
    public void ensureFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
    }

    private void gravarLista(UUID uuid, Set<String> medals) {
        ensureFile();
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        yaml.set(camPathLista(uuid), new java.util.ArrayList<>(medals));
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("[Eventos] Falha ao guardar player-medals.yml: " + e.getMessage());
        }
    }

    private static String camPathLista(UUID uuid) {
        return "players." + uuid.toString() + ".medals";
    }
}
