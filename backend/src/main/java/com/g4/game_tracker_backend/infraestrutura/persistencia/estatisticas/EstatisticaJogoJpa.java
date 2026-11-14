package com.g4.game_tracker_backend.infraestrutura.persistencia.estatisticas;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogo;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.MediasCategoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** F-17 RN 2: as médias são gravadas com precisão fixa de duas casas decimais. */
@Entity
@Table(name = "estatisticas_jogo")
public class EstatisticaJogoJpa {

    @Id
    @Column(name = "jogo_id")
    private UUID jogoId;

    @Column(name = "media_geral", precision = 3, scale = 2)
    private BigDecimal mediaGeral;

    @Column(name = "media_graficos", precision = 3, scale = 2)
    private BigDecimal mediaGraficos;

    @Column(name = "media_som", precision = 3, scale = 2)
    private BigDecimal mediaSom;

    @Column(name = "media_jogabilidade", precision = 3, scale = 2)
    private BigDecimal mediaJogabilidade;

    @Column(name = "media_diversao", precision = 3, scale = 2)
    private BigDecimal mediaDiversao;

    @Column(name = "media_inovacao", precision = 3, scale = 2)
    private BigDecimal mediaInovacao;

    @Column(name = "total_reviews_ativas", nullable = false)
    private int totalReviewsAtivas;

    @Column(name = "total_conclusoes", nullable = false)
    private int totalConclusoes;

    @Column(name = "total_em_wishlists", nullable = false)
    private int totalEmWishlists;

    @Column(name = "total_negociacoes", nullable = false)
    private int totalNegociacoes;

    @Column(name = "atualizada_em", nullable = false)
    private Instant atualizadaEm;

    protected EstatisticaJogoJpa() {
    }

    static EstatisticaJogoJpa deDominio(EstatisticaJogo estatistica) {
        EstatisticaJogoJpa jpa = new EstatisticaJogoJpa();
        MediasCategoria medias = estatistica.mediasPorCategoria();
        jpa.jogoId = estatistica.jogoId();
        jpa.mediaGeral = estatistica.mediaGeral();
        jpa.mediaGraficos = medias.graficos();
        jpa.mediaSom = medias.som();
        jpa.mediaJogabilidade = medias.jogabilidade();
        jpa.mediaDiversao = medias.diversao();
        jpa.mediaInovacao = medias.inovacao();
        jpa.totalReviewsAtivas = estatistica.totalReviewsAtivas();
        jpa.totalConclusoes = estatistica.totalConclusoes();
        jpa.totalEmWishlists = estatistica.totalEmWishlists();
        jpa.totalNegociacoes = estatistica.totalNegociacoes();
        jpa.atualizadaEm = estatistica.atualizadaEm();
        return jpa;
    }

    EstatisticaJogo paraDominio() {
        return new EstatisticaJogo(jogoId, mediaGeral,
                new MediasCategoria(mediaGraficos, mediaSom, mediaJogabilidade, mediaDiversao, mediaInovacao),
                totalReviewsAtivas, totalConclusoes, totalEmWishlists, totalNegociacoes, atualizadaEm);
    }
}
