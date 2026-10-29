package com.g4.game_tracker_backend.infraestrutura.persistencia.anuncios;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.EstadoConservacao;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.MidiaJogo;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.TipoNegociacao;

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

@Entity
@Table(name = "anuncios_marketplace", indexes = {
        @Index(name = "ix_anuncios_jogo", columnList = "jogo_id"),
        @Index(name = "ix_anuncios_vendedor", columnList = "vendedor_id")
})
public class AnuncioMarketplaceJpa {

    @Id
    private UUID id;

    @Column(name = "vendedor_id", nullable = false)
    private UUID vendedorId;

    @Column(name = "jogo_id", nullable = false)
    private UUID jogoId;

    @Column(nullable = false, length = 180)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private EstadoConservacao estado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_negociacao", nullable = false, length = 24)
    private TipoNegociacao tipoNegociacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private MidiaJogo midia;

    @Column(nullable = false, length = 120)
    private String localidade;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "anuncios_fotos", joinColumns = @JoinColumn(name = "anuncio_id"))
    private List<FotoAnuncioJpa> fotos = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private StatusAnuncio status;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected AnuncioMarketplaceJpa() {
    }

    private AnuncioMarketplaceJpa(AnuncioMarketplace anuncio) {
        this.id = anuncio.id();
        this.vendedorId = anuncio.vendedorId();
        this.jogoId = anuncio.jogoId();
        this.titulo = anuncio.titulo();
        this.estado = anuncio.estado();
        this.preco = anuncio.preco();
        this.tipoNegociacao = anuncio.tipoNegociacao();
        this.midia = anuncio.midia();
        this.localidade = anuncio.localidade();
        this.fotos = new ArrayList<>(anuncio.fotos().stream().map(FotoAnuncioJpa::deDominio).toList());
        this.status = anuncio.status();
        this.criadoEm = anuncio.criadoEm();
        this.atualizadoEm = anuncio.atualizadoEm();
    }

    static AnuncioMarketplaceJpa deDominio(AnuncioMarketplace anuncio) {
        return new AnuncioMarketplaceJpa(anuncio);
    }

    AnuncioMarketplace paraDominio() {
        return AnuncioMarketplace.reconstituir(id, vendedorId, jogoId, titulo, estado, preco, tipoNegociacao,
                midia, localidade, fotos.stream()
                        .sorted(Comparator.comparingInt(FotoAnuncioJpa::ordem))
                        .map(FotoAnuncioJpa::paraDominio)
                        .toList(),
                status, criadoEm, atualizadoEm);
    }
}
