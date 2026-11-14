package com.g4.game_tracker_backend.aplicacao.avaliacao.estatisticas;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.MetricasDoJogoAlteradas;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioStatusAlterado;

/**
 * F-17 RN 1: recalcula a ficha do jogo quando os dados de origem mudam. Roda depois do commit para
 * enxergar os dados já gravados e para que uma falha aqui não desfaça a ação do usuário.
 */
@Component
public class ObservadorEstatisticaJogo {

    private final RecalcularEstatisticas recalcular;

    public ObservadorEstatisticaJogo(RecalcularEstatisticas recalcular) {
        this.recalcular = recalcular;
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void aoAlterarMetricas(MetricasDoJogoAlteradas evento) {
        recalcular.executar(evento.jogoId());
    }

    /** O volume de negociações conta os anúncios vendidos; VENDIDO é um status final. */
    @TransactionalEventListener(fallbackExecution = true)
    public void aoAlterarStatusDoAnuncio(AnuncioStatusAlterado evento) {
        if (evento.vendido()) {
            recalcular.executar(evento.jogoId());
        }
    }
}
