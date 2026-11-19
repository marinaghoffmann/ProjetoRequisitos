package com.g4.game_tracker_backend.infraestrutura.eventos;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.g4.game_tracker_backend.aplicacao.avaliacao.estatisticas.ObservadorEstatisticaJogo;
import com.g4.game_tracker_backend.aplicacao.journal.ObservadorEventosPlataforma;
import com.g4.game_tracker_backend.aplicacao.lancamentos.SincronizarLancamentosComCatalogo;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.MetricasDoJogoAlteradas;
import com.g4.game_tracker_backend.dominio.catalogo.DataJogoAlterada;
import com.g4.game_tracker_backend.dominio.catalogo.JogoDesativado;
import com.g4.game_tracker_backend.dominio.journal.eventos.CompraConcluida;
import com.g4.game_tracker_backend.dominio.journal.eventos.ConclusaoExcluida;
import com.g4.game_tracker_backend.dominio.journal.eventos.JogoZerado;
import com.g4.game_tracker_backend.dominio.journal.eventos.ReviewExcluida;
import com.g4.game_tracker_backend.dominio.journal.eventos.ReviewPublicada;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioStatusAlterado;

/**
 * Padrão Observer: liga os eventos publicados pela porta {@code PublicadorDeEventos} aos observadores da
 * aplicação, que são Java puro. Cada método só repassa o evento; o momento da entrega é o que muda:
 * <ul>
 * <li>{@link EventListener}: na mesma thread e na mesma transação de quem publicou (F-16 RN 1 e F-12).</li>
 * <li>{@link TransactionalEventListener}: depois do commit, ou na hora se não houver transação (F-17 RN 1).</li>
 * </ul>
 */
@Component
class OuvintesDeEventosSpring {

    private final ObservadorEventosPlataforma timeline;
    private final ObservadorEstatisticaJogo estatisticas;
    private final SincronizarLancamentosComCatalogo lancamentos;

    OuvintesDeEventosSpring(ObservadorEventosPlataforma timeline, ObservadorEstatisticaJogo estatisticas,
            SincronizarLancamentosComCatalogo lancamentos) {
        this.timeline = timeline;
        this.estatisticas = estatisticas;
        this.lancamentos = lancamentos;
    }

    // ---- F-16: timeline automática, na mesma transação ----

    @EventListener
    void aoReceber(ReviewPublicada evento) {
        timeline.aoReceberReviewPublicada(evento);
    }

    @EventListener
    void aoReceber(ReviewExcluida evento) {
        timeline.aoReceberReviewExcluida(evento);
    }

    @EventListener
    void aoReceber(JogoZerado evento) {
        timeline.aoReceberJogoZerado(evento);
    }

    @EventListener
    void aoReceber(ConclusaoExcluida evento) {
        timeline.aoReceberConclusaoExcluida(evento);
    }

    @EventListener
    void aoReceber(CompraConcluida evento) {
        timeline.aoReceberCompraConcluida(evento);
    }

    // ---- F-12: lançamentos acompanham o catálogo, na mesma transação ----

    @EventListener
    void aoReceber(JogoDesativado evento) {
        lancamentos.jogoDesativado(evento);
    }

    @EventListener
    void aoReceber(DataJogoAlterada evento) {
        lancamentos.dataAlterada(evento);
    }

    // ---- F-17: estatísticas, depois do commit ----

    @TransactionalEventListener(fallbackExecution = true)
    void aoReceber(MetricasDoJogoAlteradas evento) {
        estatisticas.aoAlterarMetricas(evento);
    }

    @TransactionalEventListener(fallbackExecution = true)
    void aoReceber(AnuncioStatusAlterado evento) {
        estatisticas.aoAlterarStatusDoAnuncio(evento);
    }
}
