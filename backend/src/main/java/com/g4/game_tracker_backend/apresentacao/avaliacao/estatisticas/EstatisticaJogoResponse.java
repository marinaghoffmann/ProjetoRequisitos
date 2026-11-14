package com.g4.game_tracker_backend.apresentacao.avaliacao.estatisticas;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogo;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.MediasCategoria;

/**
 * Ficha pública do jogo. F-17 RN 3: sem o mínimo de reviews ativas, só seguem o total de reviews e o
 * mínimo exigido; os indicadores vão null para não vazarem pela API.
 */
public record EstatisticaJogoResponse(
        UUID jogoId,
        boolean elegivel,
        int totalReviewsAtivas,
        int minimoDeReviews,
        BigDecimal mediaGeral,
        MediasCategoria mediasPorCategoria,
        Integer totalConclusoes,
        Integer totalEmWishlists,
        Integer totalNegociacoes,
        Instant atualizadaEm) {

    public static EstatisticaJogoResponse de(EstatisticaJogo ficha) {
        int minimo = EstatisticaJogo.MINIMO_DE_REVIEWS_PARA_EXIBICAO;
        if (!ficha.elegivelParaExibicao()) {
            return new EstatisticaJogoResponse(ficha.jogoId(), false, ficha.totalReviewsAtivas(), minimo, null, null,
                    null, null, null, null);
        }
        return new EstatisticaJogoResponse(ficha.jogoId(), true, ficha.totalReviewsAtivas(), minimo,
                ficha.mediaGeral(), ficha.mediasPorCategoria(), ficha.totalConclusoes(), ficha.totalEmWishlists(),
                ficha.totalNegociacoes(), ficha.atualizadaEm());
    }
}
