package com.g4.game_tracker_backend.dominio.descoberta.similaridade;

import java.math.BigDecimal;

/** Estratégia (Strategy, GoF): 1 quando os dois jogos são do mesmo estúdio (desenvolvedora), 0 caso contrário. */
public final class SimilaridadePorEstudioStrategy implements SimilaridadeStrategy {

    @Override
    public BigDecimal calcular(PerfilJogo jogoA, PerfilJogo jogoB) {
        boolean mesmoEstudio = jogoA.desenvolvedoraId() != null
                && jogoA.desenvolvedoraId().equals(jogoB.desenvolvedoraId());
        return mesmoEstudio ? BigDecimal.ONE : BigDecimal.ZERO;
    }
}
