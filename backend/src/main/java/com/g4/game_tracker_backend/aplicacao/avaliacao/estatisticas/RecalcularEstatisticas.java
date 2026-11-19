package com.g4.game_tracker_backend.aplicacao.avaliacao.estatisticas;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogo;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogoRepositorio;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.FonteDeConclusoes;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.FonteDeReviews;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.FonteDeWishlists;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.NotasDoJogo;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional.Propagacao;

/**
 * F-17 RN 1: refaz a ficha do jogo a partir das fontes de F-02 (reviews), F-06 (wishlist), F-11 (conclusões)
 * e F-18 (anúncios vendidos).
 *
 * Usa transação própria porque o observador roda depois do commit da ação que gerou o evento.
 */
@CasoDeUso
public class RecalcularEstatisticas {

    private final FonteDeReviews reviews;
    private final FonteDeConclusoes conclusoes;
    private final FonteDeWishlists wishlists;
    private final AnuncioMarketplaceRepositorio anuncios;
    private final EstatisticaJogoRepositorio estatisticas;

    public RecalcularEstatisticas(FonteDeReviews reviews,
            FonteDeConclusoes conclusoes, FonteDeWishlists wishlists,
            AnuncioMarketplaceRepositorio anuncios, EstatisticaJogoRepositorio estatisticas) {
        this.reviews = reviews;
        this.conclusoes = conclusoes;
        this.wishlists = wishlists;
        this.anuncios = anuncios;
        this.estatisticas = estatisticas;
    }

    @Transacional(propagacao = Propagacao.NOVA)
    public EstatisticaJogo executar(UUID jogoId) {
        NotasDoJogo notas = reviews.notasDoJogo(jogoId);
        int totalConclusoes = conclusoes.contarConclusoes(jogoId);
        int totalEmWishlists = wishlists.contarPresencaEmWishlists(jogoId);
        int totalNegociacoes = anuncios.contarPorJogoEStatus(jogoId, StatusAnuncio.VENDIDO);

        return estatisticas.salvar(EstatisticaJogo.consolidar(jogoId, notas, totalConclusoes, totalEmWishlists,
                totalNegociacoes, Instant.now(Clock.systemUTC())));
    }
}
