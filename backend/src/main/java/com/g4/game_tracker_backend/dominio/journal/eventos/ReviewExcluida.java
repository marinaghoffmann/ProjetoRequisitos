package com.g4.game_tracker_backend.dominio.journal.eventos;

/** Evento de plataforma (F-16) que a F-02 publica ao excluir uma review, dentro da transação do caso de uso. */
public record ReviewExcluida(String reviewId) {
}
