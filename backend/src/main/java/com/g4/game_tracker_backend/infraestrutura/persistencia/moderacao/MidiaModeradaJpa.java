package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Endereço de arquivo de um conteúdo moderado, protegido mesmo que o autor edite o conteúdo depois. */
@Entity
@Table(name = "moderacao_midias",
        uniqueConstraints = @UniqueConstraint(name = "uk_moderacao_midia_alvo_url",
                columnNames = { "tipo", "conteudo_id", "url" }),
        indexes = @Index(name = "ix_moderacao_midia_url", columnList = "url"))
public class MidiaModeradaJpa {

    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TipoConteudo tipo;
    @Column(name = "conteudo_id", nullable = false)
    private UUID conteudoId;
    @Column(nullable = false, length = 1000)
    private String url;

    protected MidiaModeradaJpa() {
    }

    MidiaModeradaJpa(TipoConteudo tipo, UUID conteudoId, String url) {
        this.id = UUID.randomUUID();
        this.tipo = tipo;
        this.conteudoId = conteudoId;
        this.url = url;
    }

    public TipoConteudo tipo() {
        return tipo;
    }

    public UUID conteudoId() {
        return conteudoId;
    }
}
