package com.g4.game_tracker_backend.dominio.lancamentos;
import java.time.Instant;
import java.util.UUID;
public record NotificacaoLancamento(UUID id, UUID interesseId, Instant dispararEm, boolean enviada) {
    public NotificacaoLancamento marcarEnviada() {
        return new NotificacaoLancamento(id, interesseId, dispararEm, true);
    }
}
