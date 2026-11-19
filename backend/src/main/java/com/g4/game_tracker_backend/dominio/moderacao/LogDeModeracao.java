package com.g4.game_tracker_backend.dominio.moderacao;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Registro de auditoria de uma decisão. Só é criado, nunca alterado nem apagado. */
public record LogDeModeracao(UUID id, UUID moderadorId, AcaoModeracao acao, TipoConteudo tipo, UUID conteudoId,
        String justificativa, Instant registradoEm) {

    public LogDeModeracao {
        Objects.requireNonNull(id);
        Objects.requireNonNull(moderadorId);
        Objects.requireNonNull(acao);
        Objects.requireNonNull(registradoEm);
    }

    public static LogDeModeracao registrar(UUID moderadorId, AcaoModeracao acao, TipoConteudo tipo, UUID conteudoId,
            String justificativa, Instant agora) {
        return new LogDeModeracao(UUID.randomUUID(), moderadorId, acao, tipo, conteudoId, justificativa, agora);
    }
}
