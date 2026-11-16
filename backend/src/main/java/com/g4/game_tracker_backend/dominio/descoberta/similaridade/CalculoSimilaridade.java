package com.g4.game_tracker_backend.dominio.descoberta.similaridade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Contexto do padrão Strategy (GoF): combina as estratégias de similaridade com pesos e devolve o grau de
 * relevância entre dois jogos, de 0 a 1 com duas casas (HALF_UP).
 *
 * A combinação padrão é gênero 0,5, tags 0,3 e estúdio 0,2. Como cada estratégia é simétrica, o grau de
 * (A, B) é igual ao de (B, A) (RN 1).
 */
public final class CalculoSimilaridade {

    /** Uma estratégia e o seu peso na combinação. */
    public record Criterio(SimilaridadeStrategy estrategia, BigDecimal peso) {

        public Criterio {
            Objects.requireNonNull(estrategia, "A estratégia é obrigatória");
            Objects.requireNonNull(peso, "O peso é obrigatório");
        }
    }

    private final List<Criterio> criterios;

    public CalculoSimilaridade(List<Criterio> criterios) {
        this.criterios = List.copyOf(criterios);
        BigDecimal soma = this.criterios.stream().map(Criterio::peso).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (this.criterios.isEmpty() || soma.compareTo(BigDecimal.ONE) != 0) {
            throw new RegraDeNegocioException("Os pesos da similaridade devem somar 1");
        }
    }

    public static CalculoSimilaridade padrao() {
        return new CalculoSimilaridade(List.of(
                new Criterio(new SimilaridadePorGeneroStrategy(), new BigDecimal("0.5")),
                new Criterio(new SimilaridadePorTagStrategy(), new BigDecimal("0.3")),
                new Criterio(new SimilaridadePorEstudioStrategy(), new BigDecimal("0.2"))));
    }

    public BigDecimal calcular(PerfilJogo jogoA, PerfilJogo jogoB) {
        BigDecimal grau = criterios.stream()
                .map(criterio -> criterio.estrategia().calcular(jogoA, jogoB).multiply(criterio.peso()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return grau.max(BigDecimal.ZERO).min(BigDecimal.ONE).setScale(2, RoundingMode.HALF_UP);
    }
}
