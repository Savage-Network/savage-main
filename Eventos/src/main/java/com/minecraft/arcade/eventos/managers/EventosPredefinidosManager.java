package com.minecraft.arcade.eventos.managers;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista fixa definida em Java (substitui config {@code eventos-predefinidos}).
 */
public class EventosPredefinidosManager {

    private final List<EventoPredefinido> eventos = new ArrayList<>();

    public EventosPredefinidosManager() {
    }

    public void carregar() {
        eventos.clear();
        /* Ordem para /lm evento list (1-based). Mantém nome exibível + formato de grupo. */
        eventos.add(new EventoPredefinido("Guerra dos Clans", EventoTipo.PANELA));
        eventos.add(new EventoPredefinido("Duplas Ranked", EventoTipo.DUPLAS));
        eventos.add(new EventoPredefinido("Solo Survival", EventoTipo.SOLO));
        eventos.add(new EventoPredefinido("Trio Cup", EventoTipo.TRIO));
        eventos.add(new EventoPredefinido("Panela Masters", EventoTipo.PANELA));
        eventos.add(new EventoPredefinido("FFA Savage", EventoTipo.SOLO));
        eventos.add(new EventoPredefinido("Battle Royale", EventoTipo.SOLO));
        eventos.add(new EventoPredefinido("Last Man Standing", EventoTipo.SOLO));
        eventos.add(new EventoPredefinido("Duelos 2v2", EventoTipo.DUPLAS));
        eventos.add(new EventoPredefinido("Duplinha da Resenha", EventoTipo.DUPLAS));
        eventos.add(new EventoPredefinido("Esquadrão Savage", EventoTipo.TRIO));
        eventos.add(new EventoPredefinido("Trio Lendário", EventoTipo.TRIO));
        eventos.add(new EventoPredefinido("Família Unida", EventoTipo.PANELA));
        eventos.add(new EventoPredefinido("Panelinha da Madrugada", EventoTipo.PANELA));
        eventos.add(new EventoPredefinido("Clã vs Todo Mundo", EventoTipo.PANELA));
        eventos.add(new EventoPredefinido("Corrida até o Topo", EventoTipo.SOLO));
        eventos.add(new EventoPredefinido("Noite Ranked Duo", EventoTipo.DUPLAS));
        eventos.add(new EventoPredefinido("Domingo de Trio", EventoTipo.TRIO));
        eventos.add(new EventoPredefinido("Copa Savage", EventoTipo.TRIO));
    }

    public List<EventoPredefinido> getEventos() {
        return new ArrayList<>(eventos);
    }

    /** Número 1-based. */
    public EventoPredefinido getPorNumero(int numero) {
        if (numero < 1 || numero > eventos.size()) return null;
        return eventos.get(numero - 1);
    }

    public int getQuantidade() {
        return eventos.size();
    }

    public static class EventoPredefinido {
        private final String nome;
        private final EventoTipo tipo;

        public EventoPredefinido(String nome, EventoTipo tipo) {
            this.nome = nome != null ? nome : "Evento";
            this.tipo = tipo != null ? tipo : EventoTipo.SOLO;
        }

        public String getNome() {
            return nome;
        }

        public EventoTipo getTipo() {
            return tipo;
        }
    }
}
