package com.g4.game_tracker_backend.infraestrutura.persistencia.negociacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.marketplace.negociacao.PropostaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.StatusProposta;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.TipoProposta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/** O índice por status e data de resposta atende a rotina de expiração das reservas (RN 10). */
@Entity
@Table(name = "marketplace_propostas", indexes = {
        @Index(name = "ix_marketplace_propostas_status", columnList = "status, respondida_em")
})
public class PropostaNegociacaoJpa {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TipoProposta tipo;

    @Column(name = "valor_oferecido", precision = 12, scale = 2)
    private BigDecimal valorOferecido;

    @Column(name = "jogo_oferecido", length = 80)
    private String jogoOferecido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StatusProposta status;

    @Column(length = 80)
    private String motivo;

    @Column(name = "pedido_id")
    private UUID pedidoId;

    @Column(name = "enviada_em", nullable = false)
    private Instant enviadaEm;

    @Column(name = "respondida_em")
    private Instant respondidaEm;

    protected PropostaNegociacaoJpa() {
    }

    private PropostaNegociacaoJpa(PropostaNegociacao proposta) {
        this.id = proposta.id();
        this.tipo = proposta.tipo();
        this.valorOferecido = proposta.valorOferecido();
        this.jogoOferecido = proposta.jogoOferecido();
        this.status = proposta.status();
        this.motivo = proposta.motivo();
        this.pedidoId = proposta.pedidoId();
        this.enviadaEm = proposta.enviadaEm();
        this.respondidaEm = proposta.respondidaEm();
    }

    static PropostaNegociacaoJpa deDominio(PropostaNegociacao proposta) {
        return new PropostaNegociacaoJpa(proposta);
    }

    PropostaNegociacao paraDominio() {
        return PropostaNegociacao.reconstituir(id, tipo, valorOferecido, jogoOferecido, status, motivo, pedidoId,
                enviadaEm, respondidaEm);
    }

    Instant enviadaEm() {
        return enviadaEm;
    }
}
