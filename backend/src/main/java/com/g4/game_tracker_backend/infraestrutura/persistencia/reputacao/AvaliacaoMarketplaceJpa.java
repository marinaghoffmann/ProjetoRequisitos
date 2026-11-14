package com.g4.game_tracker_backend.infraestrutura.persistencia.reputacao;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.marketplace.reputacao.AvaliacaoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.PapelNaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.TagComportamento;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** A restrição única (pedido_id, avaliador_id) garante a RN 2 mesmo com duas requisições simultâneas. */
@Entity
@Table(name = "marketplace_avaliacoes", indexes = {
        @Index(name = "ix_marketplace_avaliacoes_avaliado", columnList = "avaliado_id, criada_em")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_marketplace_avaliacoes_pedido_avaliador",
                columnNames = { "pedido_id", "avaliador_id" })
})
public class AvaliacaoMarketplaceJpa {

    @Id
    private UUID id;

    @Column(name = "pedido_id", nullable = false)
    private UUID pedidoId;

    @Column(name = "avaliador_id", nullable = false)
    private UUID avaliadorId;

    @Column(name = "avaliado_id", nullable = false)
    private UUID avaliadoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel_avaliado", nullable = false, length = 16)
    private PapelNaNegociacao papelAvaliado;

    @Column(nullable = false)
    private int estrelas;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "marketplace_avaliacoes_tags", joinColumns = @JoinColumn(name = "avaliacao_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tag", nullable = false, length = 32)
    private Set<TagComportamento> tags = new HashSet<>();

    @Column(length = AvaliacaoMarketplace.LIMITE_DO_COMENTARIO)
    private String comentario;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    protected AvaliacaoMarketplaceJpa() {
    }

    private AvaliacaoMarketplaceJpa(AvaliacaoMarketplace avaliacao) {
        this.id = avaliacao.id();
        this.pedidoId = avaliacao.pedidoId();
        this.avaliadorId = avaliacao.avaliadorId();
        this.avaliadoId = avaliacao.avaliadoId();
        this.papelAvaliado = avaliacao.papelAvaliado();
        this.estrelas = avaliacao.estrelas();
        this.tags = new HashSet<>(avaliacao.tags());
        this.comentario = avaliacao.comentario();
        this.criadaEm = avaliacao.criadaEm();
    }

    static AvaliacaoMarketplaceJpa deDominio(AvaliacaoMarketplace avaliacao) {
        return new AvaliacaoMarketplaceJpa(avaliacao);
    }

    AvaliacaoMarketplace paraDominio() {
        return AvaliacaoMarketplace.reconstituir(id, pedidoId, avaliadorId, avaliadoId, papelAvaliado, estrelas, tags,
                comentario, criadaEm);
    }
}
