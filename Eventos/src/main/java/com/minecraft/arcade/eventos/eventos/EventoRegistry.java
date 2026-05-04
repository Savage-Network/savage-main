package com.minecraft.arcade.eventos.eventos;

import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.eventos.folha.FolhaEvento;
import com.minecraft.arcade.eventos.eventos.lavarun.LavaRunEvento;
import com.minecraft.arcade.eventos.eventos.spleef.SpleefEvento;
import com.minecraft.arcade.eventos.eventos.sumo.SumoEvento;
import com.minecraft.arcade.eventos.eventos.tntrun.TNTRunEvento;
import com.minecraft.arcade.eventos.eventos.vidro.VidroEvento;

import java.util.HashMap;
import java.util.Map;

/**
 * Registro dos eventos prontos: /lm spleef, folha, sumo, lavarun, tntrun, vidro
 */
public class EventoRegistry {

    private final Map<String, EventoPronto> porId = new HashMap<>();

    public EventoRegistry(Eventos plugin) {
        registrar(new SpleefEvento(plugin));
        registrar(new FolhaEvento(plugin));
        registrar(new SumoEvento(plugin));
        registrar(new LavaRunEvento(plugin));
        registrar(new VidroEvento(plugin));
        registrar(new TNTRunEvento(plugin));
    }

    private void registrar(EventoPronto evento) {
        porId.put(evento.getId().toLowerCase(), evento);
    }

    public EventoPronto get(String id) {
        return id != null ? porId.get(id.toLowerCase()) : null;
    }

    public Map<String, EventoPronto> getAll() {
        return new HashMap<>(porId);
    }
}
