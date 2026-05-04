package com.minecraft.arcade.eventos.managers;

import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.settings.EventEmbeddedConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Variáveis de runtime (substitui YAML). Editáveis com /var; /evtreload volta ao Java embutido.
 */
public class VariablesManager {

    private final Eventos plugin;
    private final Map<String, String> vars = new ConcurrentHashMap<>();

    public VariablesManager(Eventos plugin) {
        this.plugin = plugin;
    }

    public void carregar() {
        vars.clear();
        vars.putAll(EventEmbeddedConfig.mutableDefaultVariablesCopy());
    }

    /** Recarrega apenas os valores padrão de {@link EventEmbeddedConfig} (similar ao reload antigo de config). */
    public void reiniciarPadraoEmbutido() {
        carregar();
    }

    public String get(String key) {
        return vars.getOrDefault(normalize(key), "");
    }

    public String get(String key, String padrao) {
        String v = vars.get(normalize(key));
        return v != null && !v.isEmpty() ? v : padrao;
    }

    public void set(String key, String value) {
        if (value == null) value = "";
        vars.put(normalize(key), value);
    }

    public boolean has(String key) {
        return vars.containsKey(normalize(key));
    }

    private static String normalize(String key) {
        return key == null ? "" : key.toLowerCase();
    }

    public int getInt(String key, int padrao) {
        String v = vars.get(normalize(key));
        if (v == null || v.isEmpty()) return padrao;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    public boolean getBoolean(String key, boolean padrao) {
        String v = vars.get(normalize(key));
        if (v == null || v.isEmpty()) return padrao;
        return "true".equalsIgnoreCase(v.trim()) || "1".equals(v.trim());
    }
}
