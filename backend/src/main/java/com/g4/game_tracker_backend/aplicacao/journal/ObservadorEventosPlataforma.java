package com.g4.game_tracker_backend.aplicacao.journal;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.journal.TipoEventoPlataforma;
import com.g4.game_tracker_backend.dominio.journal.eventos.CompraConcluida;
import com.g4.game_tracker_backend.dominio.journal.eventos.ConclusaoExcluida;
import com.g4.game_tracker_backend.dominio.journal.eventos.JogoZerado;
import com.g4.game_tracker_backend.dominio.journal.eventos.ReviewExcluida;
import com.g4.game_tracker_backend.dominio.journal.eventos.ReviewPublicada;

/**
 * Observer (GoF) da F-16: escuta os eventos de plataforma publicados por reviews (F-02), conclusões (F-11)
 * e pedidos (F-20) e mantém as entradas automáticas do journal.
 *
 * Usa @EventListener, e não @TransactionalEventListener: o Spring entrega o evento na mesma thread e na
 * mesma transação de quem publicou, que é o que a RN 1 exige (gravação e rollback junto com a ação).
 */
@Component
public class ObservadorEventosPlataforma {

    private final GerarEntradaAutomatica gerar;

    public ObservadorEventosPlataforma(GerarEntradaAutomatica gerar) {
        this.gerar = gerar;
    }

    @EventListener
    public void aoReceberReviewPublicada(ReviewPublicada evento) {
        gerar.gerar(evento.autorId(), evento.jogoId(), TipoEventoPlataforma.REVIEW_PUBLICADA, evento.reviewId(),
                evento.publicadaEm());
    }

    @EventListener
    public void aoReceberReviewExcluida(ReviewExcluida evento) {
        gerar.sincronizarExclusao(TipoEventoPlataforma.REVIEW_PUBLICADA, evento.reviewId());
    }

    @EventListener
    public void aoReceberJogoZerado(JogoZerado evento) {
        gerar.gerar(evento.jogadorId(), evento.jogoId(), TipoEventoPlataforma.JOGO_ZERADO, evento.registroId(),
                evento.zeradoEm());
    }

    @EventListener
    public void aoReceberConclusaoExcluida(ConclusaoExcluida evento) {
        gerar.sincronizarExclusao(TipoEventoPlataforma.JOGO_ZERADO, evento.registroId());
    }

    @EventListener
    public void aoReceberCompraConcluida(CompraConcluida evento) {
        gerar.gerar(evento.compradorId(), evento.jogoId(), TipoEventoPlataforma.COMPRA_CONCLUIDA, evento.pedidoId(),
                evento.concluidaEm());
    }
}
