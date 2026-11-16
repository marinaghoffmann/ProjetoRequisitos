package com.g4.game_tracker_backend.infraestrutura.persistencia.similaridade;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.descoberta.similaridade.FeedbackDeSugestao;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.TipoFeedback;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** RN 2: um feedback por usuário e sugestão, com restrição única (usuario_id, similaridade_id). */
@Entity
@Table(name = "similaridades_feedbacks", uniqueConstraints = @UniqueConstraint(
        name = "uk_similaridades_feedbacks_usuario", columnNames = { "usuario_id", "similaridade_id" }),
        indexes = @Index(name = "ix_similaridades_feedbacks_sugestao", columnList = "similaridade_id"))
public class FeedbackSugestaoJpa {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "similaridade_id", nullable = false)
    private UUID similaridadeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TipoFeedback tipo;

    @Column(name = "registrado_em", nullable = false)
    private Instant registradoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected FeedbackSugestaoJpa() {
    }

    private FeedbackSugestaoJpa(FeedbackDeSugestao feedback) {
        this.id = feedback.id();
        this.usuarioId = feedback.usuarioId();
        this.similaridadeId = feedback.similaridadeId();
        this.tipo = feedback.tipo();
        this.registradoEm = feedback.registradoEm();
        this.atualizadoEm = feedback.atualizadoEm();
    }

    static FeedbackSugestaoJpa deDominio(FeedbackDeSugestao feedback) {
        return new FeedbackSugestaoJpa(feedback);
    }

    FeedbackDeSugestao paraDominio() {
        return FeedbackDeSugestao.reconstituir(id, usuarioId, similaridadeId, tipo, registradoEm, atualizadoEm);
    }
}
