package com.g4.game_tracker_backend.dominio.journal.eventos;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de plataforma (F-16) que a F-02 publica ao criar uma review, dentro da transação do caso de uso.
 * reviewId é o id da review, usado para remover a entrada se a review for excluída.
 */
public record ReviewPublicada(UUID autorId, UUID jogoId, String reviewId, Instant publicadaEm) {
}
