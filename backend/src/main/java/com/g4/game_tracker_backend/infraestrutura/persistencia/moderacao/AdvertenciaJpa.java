package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.moderacao.Advertencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "moderacao_advertencias", indexes = @Index(name = "ix_advertencias_usuario", columnList = "usuario_id"))
class AdvertenciaJpa {

    @Id
    private UUID id;
    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;
    @Column(name = "moderador_id", nullable = false)
    private UUID moderadorId;
    @Column(nullable = false, length = 1000)
    private String motivo;
    @Column(name = "registrada_em", nullable = false)
    private Instant registradaEm;

    protected AdvertenciaJpa() {
    }

    static AdvertenciaJpa deDominio(Advertencia a) {
        AdvertenciaJpa j = new AdvertenciaJpa();
        j.id = a.id();
        j.usuarioId = a.usuarioId();
        j.moderadorId = a.moderadorId();
        j.motivo = a.motivo();
        j.registradaEm = a.registradaEm();
        return j;
    }

    Advertencia paraDominio() {
        return new Advertencia(id, usuarioId, moderadorId, motivo, registradaEm);
    }
}
