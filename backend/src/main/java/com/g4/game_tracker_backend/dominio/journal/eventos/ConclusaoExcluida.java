package com.g4.game_tracker_backend.dominio.journal.eventos;

/** Evento de plataforma (F-16) que a F-11 publica ao excluir um registro de conclusão, dentro da transação. */
public record ConclusaoExcluida(String registroId) {
}
