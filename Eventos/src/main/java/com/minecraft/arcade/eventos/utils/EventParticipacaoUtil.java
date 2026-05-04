package com.minecraft.arcade.eventos.utils;

import com.minecraft.core.bukkit.api.vanish.Vanish;
import org.bukkit.entity.Player;

/**
 * Quem conta como “dentro” do servidor de eventos ao entrar de rede / ser considerado para fila automática:
 * permissão {@code eventos.jogar} e fora do vanish da staff ({@link Vanish}).
 */
public final class EventParticipacaoUtil {

    private EventParticipacaoUtil() {
    }

    /** Jogador visível ao público elegível ao evento (não conta staff em vanish). */
    public static boolean contaComoJogadorElegivelEvento(Player p) {
        return p != null
                && p.isOnline()
                && p.hasPermission("eventos.jogar")
                && !Vanish.has(p);
    }
}
