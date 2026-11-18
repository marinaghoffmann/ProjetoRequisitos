package com.g4.game_tracker_backend.infraestrutura.eventos;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.compartilhado.PublicadorDeEventos;

/**
 * Adapter da porta {@link PublicadorDeEventos} sobre os eventos do Spring. O Spring entrega o evento
 * síncrono, na mesma thread e na mesma transação de quem publicou, aos observadores registrados (F-16 e F-17).
 */
@Component
public class PublicadorDeEventosSpring implements PublicadorDeEventos {

    private final ApplicationEventPublisher eventos;

    public PublicadorDeEventosSpring(ApplicationEventPublisher eventos) {
        this.eventos = eventos;
    }

    @Override
    public void publicar(Object evento) {
        eventos.publishEvent(evento);
    }
}
