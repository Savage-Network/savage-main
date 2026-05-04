package com.minecraft.arcade.eventos.eventos;

import com.minecraft.arcade.eventos.managers.EventoTipo;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * Contrato para um evento pronto com geração de mapa e regras próprias.
 * Cada evento (spleef, folha, sumo, etc.) implementa esta interface.
 */
public interface EventoPronto {

    /** Id do comando: /lm spleef -> "spleef" */
    String getId();

    /** Nome exibido (scoreboard, mensagens) */
    String getNome();

    /** Solo, duplas, trio ou panela */
    EventoTipo getTipo();

    /**
     * Gera o mapa na origem e retorna a Location de spawn dos jogadores.
     * Chamado ao iniciar o evento.
     */
    Location prepararMapa(World world, Location origin);

    /**
     * Remove/restaura a arena. Chamado ao parar ou finalizar o evento.
     */
    void limparMapa();

    /**
     * Registra os listeners específicos deste evento no plugin.
     */
    void registerListeners(Plugin plugin);

    /**
     * Desregistra listeners. Chamado ao parar o evento.
     */
    void unregisterListeners();
}
