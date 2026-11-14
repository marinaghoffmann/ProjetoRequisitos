package com.g4.game_tracker_backend.aplicacao.avaliacao.estatisticas;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogo;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogoRepositorio;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.FonteDeConclusoes;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.FonteDeReviews;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.FonteDeWishlists;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.NotasDoJogo;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;

/**
 * F-17 RN 1: refaz a ficha do jogo a partir das fontes. As fontes de F-02, F-06 e F-11 são opcionais
 * porque essas funcionalidades ainda não existem; o marketplace já está disponível.
 *
 * Usa transação própria porque o observador roda depois do commit da ação que gerou o evento.
 */
@Service
public class RecalcularEstatisticas {

    private final ObjectProvider<FonteDeReviews> reviews;
    private final ObjectProvider<FonteDeConclusoes> conclusoes;
    private final ObjectProvider<FonteDeWishlists> wishlists;
    private final AnuncioMarketplaceRepositorio anuncios;
    private final EstatisticaJogoRepositorio estatisticas;

    public RecalcularEstatisticas(ObjectProvider<FonteDeReviews> reviews,
            ObjectProvider<FonteDeConclusoes> conclusoes, ObjectProvider<FonteDeWishlists> wishlists,
            AnuncioMarketplaceRepositorio anuncios, EstatisticaJogoRepositorio estatisticas) {
        this.reviews = reviews;
        this.conclusoes = conclusoes;
        this.wishlists = wishlists;
        this.anuncios = anuncios;
        this.estatisticas = estatisticas;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EstatisticaJogo executar(UUID jogoId) {
        FonteDeReviews fonteDeReviews = reviews.getIfAvailable();
        FonteDeConclusoes fonteDeConclusoes = conclusoes.getIfAvailable();
        FonteDeWishlists fonteDeWishlists = wishlists.getIfAvailable();

        NotasDoJogo notas = fonteDeReviews == null ? NotasDoJogo.vazias() : fonteDeReviews.notasDoJogo(jogoId);
        int totalConclusoes = fonteDeConclusoes == null ? 0 : fonteDeConclusoes.contarConclusoes(jogoId);
        int totalEmWishlists = fonteDeWishlists == null ? 0 : fonteDeWishlists.contarPresencaEmWishlists(jogoId);
        int totalNegociacoes = anuncios.contarPorJogoEStatus(jogoId, StatusAnuncio.VENDIDO);

        return estatisticas.salvar(EstatisticaJogo.consolidar(jogoId, notas, totalConclusoes, totalEmWishlists,
                totalNegociacoes, Instant.now(Clock.systemUTC())));
    }
}
