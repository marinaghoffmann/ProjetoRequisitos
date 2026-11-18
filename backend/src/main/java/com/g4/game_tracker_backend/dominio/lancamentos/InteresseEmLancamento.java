package com.g4.game_tracker_backend.dominio.lancamentos;
import java.time.Instant;
import java.util.UUID;
public record InteresseEmLancamento(UUID id, UUID usuarioId, UUID jogoId, boolean ativo, Instant registradoEm) {
    public InteresseEmLancamento comAtivo(boolean valor, Instant agora) {
        return new InteresseEmLancamento(id, usuarioId, jogoId, valor, agora);
    }
}
