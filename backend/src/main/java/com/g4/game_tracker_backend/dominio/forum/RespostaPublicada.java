package com.g4.game_tracker_backend.dominio.forum;

import java.time.Instant;
import java.util.UUID;

/** Evento de domínio: uma resposta nova foi publicada em um tópico (útil para timeline e notificações). */
public record RespostaPublicada(UUID topicoId, UUID respostaId, UUID autorId, Instant publicadaEm) {
}
