package com.g4.game_tracker_backend.dominio.journal.eventos;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de plataforma (F-16) que a F-11 publica ao registrar uma conclusão, dentro da transação do caso
 * de uso. registroId é o id do registro de conclusão.
 */
public record JogoZerado(UUID jogadorId, UUID jogoId, String registroId, Instant zeradoEm) {
}
