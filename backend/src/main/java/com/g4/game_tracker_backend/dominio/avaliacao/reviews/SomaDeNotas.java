package com.g4.game_tracker_backend.dominio.avaliacao.reviews;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Soma e quantidade de notas: a média sai daqui sem acumular erro de arredondamento. */
public record SomaDeNotas(long soma, int quantidade) {

    public static SomaDeNotas vazia() {
        return new SomaDeNotas(0, 0);
    }

    /** Média com duas casas decimais (HALF_UP), como a F-17; sem notas, null. */
    public BigDecimal media() {
        if (quantidade == 0) {
            return null;
        }
        return BigDecimal.valueOf(soma).divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.HALF_UP);
    }
}
