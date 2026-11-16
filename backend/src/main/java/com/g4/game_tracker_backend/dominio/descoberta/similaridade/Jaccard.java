package com.g4.game_tracker_backend.dominio.descoberta.similaridade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

/** Índice de Jaccard: tamanho da interseção sobre o tamanho da união. Dois conjuntos vazios valem 0. */
final class Jaccard {

    private Jaccard() {
    }

    static BigDecimal indice(Set<String> a, Set<String> b) {
        Set<String> uniao = new HashSet<>(a);
        uniao.addAll(b);
        if (uniao.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Set<String> intersecao = new HashSet<>(a);
        intersecao.retainAll(b);
        return BigDecimal.valueOf(intersecao.size()).divide(BigDecimal.valueOf(uniao.size()), 4, RoundingMode.HALF_UP);
    }
}
