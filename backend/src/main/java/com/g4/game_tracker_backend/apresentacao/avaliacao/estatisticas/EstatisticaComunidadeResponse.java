package com.g4.game_tracker_backend.apresentacao.avaliacao.estatisticas;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.avaliacao.estatisticas.ConsultarEstatisticasDaComunidade.Linha;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogo;

/**
 * Uma linha da tabela "Estatísticas da comunidade". F-17 RN 3: numa ficha oculta (menos de 3 reviews ativas)
 * só seguem o total de reviews e o mínimo exigido; os indicadores vão null, como na ficha do jogo.
 */
public record EstatisticaComunidadeResponse(
        UUID jogoId,
        String titulo,
        String capaUrl,
        List<String> generos,
        boolean publica,
        int totalReviewsAtivas,
        int minimoDeReviews,
        BigDecimal mediaGeral,
        Integer totalConclusoes,
        Integer totalEmWishlists,
        Integer totalNegociacoes,
        Instant atualizadaEm) {

    static EstatisticaComunidadeResponse de(Linha linha) {
        EstatisticaJogo ficha = linha.ficha();
        boolean publica = ficha.elegivelParaExibicao();
        return new EstatisticaComunidadeResponse(linha.jogo().id(), linha.jogo().titulo(), linha.jogo().capaUrl(),
                linha.generos(), publica, ficha.totalReviewsAtivas(), EstatisticaJogo.MINIMO_DE_REVIEWS_PARA_EXIBICAO,
                publica ? ficha.mediaGeral() : null,
                publica ? ficha.totalConclusoes() : null,
                publica ? ficha.totalEmWishlists() : null,
                publica ? ficha.totalNegociacoes() : null,
                publica ? ficha.atualizadaEm() : null);
    }
}
