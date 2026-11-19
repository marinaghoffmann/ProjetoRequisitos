package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.Immutable;

import com.g4.game_tracker_backend.dominio.moderacao.AcaoModeracao;
import com.g4.game_tracker_backend.dominio.moderacao.LogDeModeracao;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Immutable
@Table(name = "moderacao_logs", indexes = @Index(name = "ix_moderacao_logs_data", columnList = "registrado_em"))
class LogModeracaoJpa {

    @Id
    private UUID id;
    @Column(name = "moderador_id", nullable = false)
    private UUID moderadorId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AcaoModeracao acao;
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private TipoConteudo tipo;
    @Column(name = "conteudo_id")
    private UUID conteudoId;
    @Column(length = 2000)
    private String justificativa;
    @Column(name = "registrado_em", nullable = false)
    private Instant registradoEm;

    protected LogModeracaoJpa() {
    }

    static LogModeracaoJpa deDominio(LogDeModeracao log) {
        LogModeracaoJpa j = new LogModeracaoJpa();
        j.id = log.id();
        j.moderadorId = log.moderadorId();
        j.acao = log.acao();
        j.tipo = log.tipo();
        j.conteudoId = log.conteudoId();
        j.justificativa = log.justificativa();
        j.registradoEm = log.registradoEm();
        return j;
    }

    LogDeModeracao paraDominio() {
        return new LogDeModeracao(id, moderadorId, acao, tipo, conteudoId, justificativa, registradoEm);
    }
}
