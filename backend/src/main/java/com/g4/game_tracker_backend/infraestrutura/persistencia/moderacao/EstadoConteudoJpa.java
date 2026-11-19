package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.moderacao.EstadoConteudo;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Os outros contextos consultam esta tabela (por JPQL ou subconsulta) para filtrar conteúdo moderado. */
@Entity
@Table(name = "moderacao_estados",
        uniqueConstraints = @UniqueConstraint(name = "uk_moderacao_alvo", columnNames = { "tipo", "conteudo_id" }))
public class EstadoConteudoJpa {

    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TipoConteudo tipo;
    @Column(name = "conteudo_id", nullable = false)
    private UUID conteudoId;
    @Column(nullable = false)
    private boolean oculto;
    @Column(nullable = false)
    private boolean removido;
    @Column(name = "aviso_18", nullable = false)
    private boolean aviso18;
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;
    @Column(name = "status_anterior", length = 16)
    private String statusAnterior;

    protected EstadoConteudoJpa() {
    }

    static EstadoConteudoJpa deDominio(EstadoConteudo e) {
        EstadoConteudoJpa j = new EstadoConteudoJpa();
        j.id = e.id();
        j.atualizar(e);
        return j;
    }

    void atualizar(EstadoConteudo e) {
        tipo = e.tipo();
        conteudoId = e.conteudoId();
        oculto = e.oculto();
        removido = e.removido();
        aviso18 = e.aviso18();
        statusAnterior = e.statusAnterior();
        atualizadoEm = e.atualizadoEm();
    }

    EstadoConteudo paraDominio() {
        return EstadoConteudo.reconstituir(id, tipo, conteudoId, oculto, removido, aviso18, statusAnterior,
                atualizadoEm);
    }
}
