package com.g4.game_tracker_backend.dominio.descoberta.similaridade;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Feedback de um jogador sobre uma sugestão (F-10 RN 2): UTIL ou NAO_RELEVANTE. Um por jogador e
 * sugestão (checado no caso de uso e garantido no banco); só o autor edita ou remove.
 */
public final class FeedbackDeSugestao {

    public static final String JA_AVALIADA = "Você já avaliou esta sugestão";

    private final UUID id;
    private final UUID usuarioId;
    private final UUID similaridadeId;
    private final TipoFeedback tipo;
    private final Instant registradoEm;
    private final Instant atualizadoEm;

    private FeedbackDeSugestao(UUID id, UUID usuarioId, UUID similaridadeId, TipoFeedback tipo, Instant registradoEm,
            Instant atualizadoEm) {
        this.id = Objects.requireNonNull(id, "O identificador do feedback é obrigatório");
        this.usuarioId = Objects.requireNonNull(usuarioId, "O autor do feedback é obrigatório");
        this.similaridadeId = Objects.requireNonNull(similaridadeId, "A sugestão do feedback é obrigatória");
        if (tipo == null) {
            throw new RegraDeNegocioException("O tipo do feedback deve ser UTIL ou NAO_RELEVANTE");
        }
        this.tipo = tipo;
        this.registradoEm = Objects.requireNonNull(registradoEm, "A data do feedback é obrigatória");
        this.atualizadoEm = Objects.requireNonNull(atualizadoEm, "A data de atualização é obrigatória");
    }

    public static FeedbackDeSugestao registrar(UUID usuarioId, UUID similaridadeId, TipoFeedback tipo, Instant agora) {
        return new FeedbackDeSugestao(UUID.randomUUID(), usuarioId, similaridadeId, tipo, agora, agora);
    }

    public static FeedbackDeSugestao reconstituir(UUID id, UUID usuarioId, UUID similaridadeId, TipoFeedback tipo,
            Instant registradoEm, Instant atualizadoEm) {
        return new FeedbackDeSugestao(id, usuarioId, similaridadeId, tipo, registradoEm, atualizadoEm);
    }

    public FeedbackDeSugestao editar(UUID solicitanteId, TipoFeedback novoTipo, Instant agora) {
        exigirAutor(solicitanteId);
        return new FeedbackDeSugestao(id, usuarioId, similaridadeId, novoTipo, registradoEm, agora);
    }

    public void exigirAutor(UUID solicitanteId) {
        if (!usuarioId.equals(solicitanteId)) {
            throw new AcessoNegadoException("Apenas o autor pode alterar este feedback");
        }
    }

    public UUID id() { return id; }
    public UUID usuarioId() { return usuarioId; }
    public UUID similaridadeId() { return similaridadeId; }
    public TipoFeedback tipo() { return tipo; }
    public Instant registradoEm() { return registradoEm; }
    public Instant atualizadoEm() { return atualizadoEm; }
}
