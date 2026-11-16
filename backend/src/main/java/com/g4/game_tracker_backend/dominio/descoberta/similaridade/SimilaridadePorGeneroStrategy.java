package com.g4.game_tracker_backend.dominio.descoberta.similaridade;

import java.math.BigDecimal;

/** Estratégia (Strategy, GoF): Jaccard entre os gêneros dos dois jogos. */
public final class SimilaridadePorGeneroStrategy implements SimilaridadeStrategy {

    @Override
    public BigDecimal calcular(PerfilJogo jogoA, PerfilJogo jogoB) {
        return Jaccard.indice(jogoA.generos(), jogoB.generos());
    }
}
