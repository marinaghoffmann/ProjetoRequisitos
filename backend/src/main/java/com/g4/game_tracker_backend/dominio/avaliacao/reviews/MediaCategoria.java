package com.g4.game_tracker_backend.dominio.avaliacao.reviews;

import java.math.BigDecimal;

/** Média de uma categoria nas reviews de um jogo; null quando ninguém deu nota a ela. */
public record MediaCategoria(CategoriaReview categoria, BigDecimal media, int totalNotas) {
}
