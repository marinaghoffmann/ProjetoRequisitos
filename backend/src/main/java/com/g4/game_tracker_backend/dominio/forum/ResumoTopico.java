package com.g4.game_tracker_backend.dominio.forum;

import java.time.Instant;
import java.util.UUID;

/** Linha da listagem paginada de tópicos (consulta da F-13), sem carregar as respostas. */
public record ResumoTopico(UUID id, UUID autorId, UUID jogoId, String categoria, String titulo,
        StatusTopico status, int totalRespostas, Instant dataUltimaAtividade, Instant publicadoEm) {
}
