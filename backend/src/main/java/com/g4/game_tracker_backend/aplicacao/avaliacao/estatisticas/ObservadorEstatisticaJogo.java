package com.g4.game_tracker_backend.aplicacao.avaliacao.estatisticas;


import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.MetricasDoJogoAlteradas;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioStatusAlterado;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;

/**
 * F-17 RN 1: recalcula a ficha do jogo quando os dados de origem mudam. O OuvintesDeEventosSpring
 * (infraestrutura) entrega o evento depois do commit, para enxergar os dados já gravados e para que uma falha
 * aqui não desfaça a ação do usuário.
 */
@CasoDeUso
public class ObservadorEstatisticaJogo {

    private final RecalcularEstatisticas recalcular;

    public ObservadorEstatisticaJogo(RecalcularEstatisticas recalcular) {
        this.recalcular = recalcular;
    }
    public void aoAlterarMetricas(MetricasDoJogoAlteradas evento) {
        recalcular.executar(evento.jogoId());
    }

    /** O volume de negociações conta os anúncios vendidos; VENDIDO é um status final. */
    public void aoAlterarStatusDoAnuncio(AnuncioStatusAlterado evento) {
        if (evento.vendido()) {
            recalcular.executar(evento.jogoId());
        }
    }
}
