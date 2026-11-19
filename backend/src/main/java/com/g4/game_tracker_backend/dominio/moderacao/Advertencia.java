package com.g4.game_tracker_backend.dominio.moderacao;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Advertência formal de um moderador a um usuário. */
public record Advertencia(UUID id, UUID usuarioId, UUID moderadorId, String motivo, Instant registradaEm) {

    public static final int LIMITE_DO_MOTIVO = 1000;

    public Advertencia {
        Objects.requireNonNull(id);
        Objects.requireNonNull(usuarioId);
        Objects.requireNonNull(moderadorId);
        Objects.requireNonNull(registradaEm);
    }

    public static Advertencia emitir(UUID usuarioId, UUID moderadorId, String motivo, Instant agora) {
        String texto = TextoModerado.exigir(motivo, LIMITE_DO_MOTIVO, "O motivo é obrigatório");
        return new Advertencia(UUID.randomUUID(), usuarioId, moderadorId, texto, agora);
    }
}
