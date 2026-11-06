package com.g4.game_tracker_backend.dominio.avaliacao.estatisticas;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Soma e quantidade de notas, de onde sai a média sem acumular erro de arredondamento. */
public record NotasAgregadas(BigDecimal soma, int quantidade) {

    public static NotasAgregadas vazias() {
        return new NotasAgregadas(BigDecimal.ZERO, 0);
    }

    public static NotasAgregadas de(int... notas) {
        int soma = 0;
        for (int nota : notas) {
            soma += nota;
        }
        return new NotasAgregadas(BigDecimal.valueOf(soma), notas.length);
    }

    /** F-17 RN 2: duas casas decimais. Sem notas não há média, e o resultado é null. */
    public BigDecimal media() {
        if (quantidade == 0) {
            return null;
        }
        return soma.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.HALF_UP);
    }
}
