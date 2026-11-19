package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.moderacao.DenunciaConteudo;
import com.g4.game_tracker_backend.dominio.moderacao.StatusDenuncia;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "moderacao_denuncias", indexes = {
        @Index(name = "ix_denuncias_status_data", columnList = "status,registrada_em"),
        @Index(name = "ix_denuncias_alvo", columnList = "tipo,conteudo_id") })
class DenunciaJpa {

    @Id
    private UUID id;
    @Column(name = "denunciante_id", nullable = false)
    private UUID denuncianteId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TipoConteudo tipo;
    @Column(name = "conteudo_id", nullable = false)
    private UUID conteudoId;
    @Column(nullable = false, length = 1000)
    private String motivo;
    /** Único enquanto pendente: o banco barra duas pendências do mesmo jogador para o mesmo alvo. */
    @Column(name = "chave_pendente", unique = true, length = 120)
    private String chavePendente;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusDenuncia status;
    @Column(name = "registrada_em", nullable = false)
    private Instant registradaEm;
    @Column(name = "analisada_em")
    private Instant analisadaEm;

    protected DenunciaJpa() {
    }

    static DenunciaJpa deDominio(DenunciaConteudo d) {
        DenunciaJpa j = new DenunciaJpa();
        j.id = d.id();
        j.atualizar(d);
        return j;
    }

    void atualizar(DenunciaConteudo d) {
        denuncianteId = d.denuncianteId();
        tipo = d.tipo();
        conteudoId = d.conteudoId();
        motivo = d.motivo();
        status = d.status();
        chavePendente = d.pendente() ? d.denuncianteId() + ":" + d.tipo() + ":" + d.conteudoId() : null;
        registradaEm = d.registradaEm();
        analisadaEm = d.analisadaEm();
    }

    DenunciaConteudo paraDominio() {
        return DenunciaConteudo.reconstituir(id, denuncianteId, tipo, conteudoId, motivo, status, registradaEm,
                analisadaEm);
    }
}
