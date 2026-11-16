package com.g4.game_tracker_backend.dominio.avaliacao.reviews;

/**
 * Nota por categoria como chega do usuário, antes de ser validada pelo agregado. A lista pode trazer a
 * mesma categoria duas vezes; é o agregado que recusa.
 */
public record NotaInformada(CategoriaReview categoria, Integer nota) {
}
