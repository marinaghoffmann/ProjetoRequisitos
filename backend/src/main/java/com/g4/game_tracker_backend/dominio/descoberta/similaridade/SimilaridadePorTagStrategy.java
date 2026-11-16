package com.g4.game_tracker_backend.dominio.descoberta.similaridade;

import java.math.BigDecimal;

/** Estratégia (Strategy, GoF): Jaccard entre as tags dos dois jogos. */
public final class SimilaridadePorTagStrategy implements SimilaridadeStrategy {

    @Override
    public BigDecimal calcular(PerfilJogo jogoA, PerfilJogo jogoB) {
        return Jaccard.indice(jogoA.tags(), jogoB.tags());
    }
}
