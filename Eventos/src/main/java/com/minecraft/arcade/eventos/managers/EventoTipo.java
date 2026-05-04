package com.minecraft.arcade.eventos.managers;

import java.util.Locale;

/**
 * Modo do evento: solo (cada um por si), duplas (times de 2), trio (3) ou panela (4).
 */
public enum EventoTipo {
    SOLO(0),
    DUPLAS(2),
    TRIO(3),
    PANELA(4);

    private final int tamanhoTime;

    EventoTipo(int tamanhoTime) {
        this.tamanhoTime = tamanhoTime;
    }

    /** Tamanho do time (0 = solo, sem times). */
    public int getTamanhoTime() {
        return tamanhoTime;
    }

    public boolean isEmTime() {
        return tamanhoTime > 0;
    }

    /** Resolve nome do tipo ignorando espaços extras; formato inválido retorna {@code null}. */
    public static EventoTipo parseTipo(String s) {
        if (s == null) return null;
        String u = s.trim().toUpperCase(Locale.ROOT);
        switch (u) {
            case "SOLO":
                return SOLO;
            case "DUPLAS":
                return DUPLAS;
            case "TRIO":
                return TRIO;
            case "PANELA":
                return PANELA;
            default:
                return null;
        }
    }

    /** Igual ao que o servidor já usava: tipo desconhecido vira solo. */
    public static EventoTipo fromString(String s) {
        EventoTipo t = parseTipo(s);
        return t != null ? t : SOLO;
    }
}
