package com.g4.game_tracker_backend.dominio.journal.eventos;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de plataforma (F-16) que a F-20 publica quando o comprador confirma o recebimento e o pedido é
 * concluído, dentro da transação do caso de uso. pedidoId é o id do pedido.
 */
public record CompraConcluida(UUID compradorId, UUID jogoId, String pedidoId, Instant concluidaEm) {
}
