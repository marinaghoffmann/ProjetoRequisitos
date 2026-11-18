package com.g4.game_tracker_backend.dominio.avaliacao.reviews;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;

/** Consulta das reviews de um jogo: médias de todas as reviews e uma página delas. */
public record ResumoReviewsJogo(UUID jogoId, int totalReviews, BigDecimal mediaGeral,
        List<MediaCategoria> mediasPorCategoria, Pagina<Review> reviews) {

    public static ResumoReviewsJogo de(UUID jogoId, ResumoDeNotas notas, Pagina<Review> reviews) {
        return new ResumoReviewsJogo(jogoId, notas.totalReviews(), notas.geral().media(),
                notas.mediasPorCategoria(), reviews);
    }

    public BigDecimal mediaDe(CategoriaReview categoria) {
        return mediasPorCategoria.stream()
                .filter(media -> media.categoria() == categoria)
                .map(MediaCategoria::media)
                .findFirst()
                .orElse(null);
    }
}
