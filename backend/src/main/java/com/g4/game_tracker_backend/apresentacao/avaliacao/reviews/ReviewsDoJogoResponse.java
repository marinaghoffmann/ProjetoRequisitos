package com.g4.game_tracker_backend.apresentacao.avaliacao.reviews;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.apresentacao.compartilhado.PaginaResponse;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ResumoReviewsJogo;

/**
 * Reviews de um jogo: as médias (geral e por categoria, com duas casas) consideram todas as reviews, e
 * não só as da página. Categorias sem nota vêm com media null.
 */
public record ReviewsDoJogoResponse(UUID jogoId, int totalReviews, BigDecimal mediaGeral,
        List<Media> mediasPorCategoria, PaginaResponse<ReviewResponse> reviews) {

    public record Media(String categoria, BigDecimal media, int totalNotas) {
    }

    static ReviewsDoJogoResponse de(ResumoReviewsJogo resumo, PaginaResponse<ReviewResponse> reviews) {
        return new ReviewsDoJogoResponse(resumo.jogoId(), resumo.totalReviews(), resumo.mediaGeral(),
                resumo.mediasPorCategoria().stream()
                        .map(media -> new Media(media.categoria().name(), media.media(), media.totalNotas()))
                        .toList(),
                reviews);
    }
}
