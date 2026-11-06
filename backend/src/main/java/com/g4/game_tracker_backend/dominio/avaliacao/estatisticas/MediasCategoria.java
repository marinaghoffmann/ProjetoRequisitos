package com.g4.game_tracker_backend.dominio.avaliacao.estatisticas;

import java.math.BigDecimal;

/** Média de cada categoria de nota da review. Uma categoria sem notas fica null. */
public record MediasCategoria(
        BigDecimal graficos,
        BigDecimal som,
        BigDecimal jogabilidade,
        BigDecimal diversao,
        BigDecimal inovacao) {
}
