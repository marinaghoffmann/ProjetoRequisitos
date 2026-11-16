package com.g4.game_tracker_backend.dominio.descoberta.similaridade;

import java.math.BigDecimal;

/**
 * Padrão Strategy (GoF): cada critério de semelhança entre dois jogos é uma estratégia intercambiável.
 *
 * Implementações: SimilaridadePorGeneroStrategy, SimilaridadePorTagStrategy e
 * SimilaridadePorEstudioStrategy. O CalculoSimilaridade (contexto do padrão) combina as estratégias com
 * pesos, sem conhecer como cada uma calcula; um critério novo é só mais uma implementação.
 *
 * Toda estratégia é simétrica (calcular(a, b) == calcular(b, a)) e devolve um grau entre 0 e 1.
 */
public interface SimilaridadeStrategy {

    BigDecimal calcular(PerfilJogo jogoA, PerfilJogo jogoB);
}
