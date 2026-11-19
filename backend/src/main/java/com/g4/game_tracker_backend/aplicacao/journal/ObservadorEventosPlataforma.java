package com.g4.game_tracker_backend.aplicacao.journal;


import com.g4.game_tracker_backend.dominio.journal.TipoEventoPlataforma;
import com.g4.game_tracker_backend.dominio.journal.eventos.CompraConcluida;
import com.g4.game_tracker_backend.dominio.journal.eventos.ConclusaoExcluida;
import com.g4.game_tracker_backend.dominio.journal.eventos.JogoZerado;
import com.g4.game_tracker_backend.dominio.journal.eventos.ReviewExcluida;
import com.g4.game_tracker_backend.dominio.journal.eventos.ReviewPublicada;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;

/**
 * Observer (GoF) da F-16: escuta os eventos de plataforma publicados por reviews (F-02), conclusões (F-11)
 * e pedidos (F-20) e mantém as entradas automáticas do journal.
 *
 * Os eventos chegam pelo OuvintesDeEventosSpring (infraestrutura) na mesma thread e na mesma transação de
 * quem publicou, que é o que a RN 1 exige (gravação e rollback junto com a ação).
 */
@CasoDeUso
public class ObservadorEventosPlataforma {

    private final GerarEntradaAutomatica gerar;

    public ObservadorEventosPlataforma(GerarEntradaAutomatica gerar) {
        this.gerar = gerar;
    }
    public void aoReceberReviewPublicada(ReviewPublicada evento) {
        gerar.gerar(evento.autorId(), evento.jogoId(), TipoEventoPlataforma.REVIEW_PUBLICADA, evento.reviewId(),
                evento.publicadaEm());
    }
    public void aoReceberReviewExcluida(ReviewExcluida evento) {
        gerar.sincronizarExclusao(TipoEventoPlataforma.REVIEW_PUBLICADA, evento.reviewId());
    }
    public void aoReceberJogoZerado(JogoZerado evento) {
        gerar.gerar(evento.jogadorId(), evento.jogoId(), TipoEventoPlataforma.JOGO_ZERADO, evento.registroId(),
                evento.zeradoEm());
    }
    public void aoReceberConclusaoExcluida(ConclusaoExcluida evento) {
        gerar.sincronizarExclusao(TipoEventoPlataforma.JOGO_ZERADO, evento.registroId());
    }
    public void aoReceberCompraConcluida(CompraConcluida evento) {
        gerar.gerar(evento.compradorId(), evento.jogoId(), TipoEventoPlataforma.COMPRA_CONCLUIDA, evento.pedidoId(),
                evento.concluidaEm());
    }
}
