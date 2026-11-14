package com.g4.game_tracker_backend.dominio.avaliacao.estatisticas;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Indicadores consolidados de um jogo (F-17). Existe no máximo uma por jogo e é sempre recalculada
 * por inteiro a partir das fontes, nunca incrementada (RN 1).
 *
 * @param atualizadaEm null enquanto o jogo nunca foi recalculado
 */
public record EstatisticaJogo(
        UUID jogoId,
        BigDecimal mediaGeral,
        MediasCategoria mediasPorCategoria,
        int totalReviewsAtivas,
        int totalConclusoes,
        int totalEmWishlists,
        int totalNegociacoes,
        Instant atualizadaEm) {

    /** F-17 RN 3: abaixo disso as estatísticas não são exibidas ao público. */
    public static final int MINIMO_DE_REVIEWS_PARA_EXIBICAO = 3;

    public EstatisticaJogo {
        Objects.requireNonNull(jogoId, "O jogo é obrigatório");
        Objects.requireNonNull(mediasPorCategoria, "As médias por categoria são obrigatórias");
    }

    public static EstatisticaJogo consolidar(UUID jogoId, NotasDoJogo notas, int totalConclusoes,
            int totalEmWishlists, int totalNegociacoes, Instant agora) {
        MediasCategoria medias = new MediasCategoria(notas.graficos().media(), notas.som().media(),
                notas.jogabilidade().media(), notas.diversao().media(), notas.inovacao().media());
        return new EstatisticaJogo(jogoId, notas.geral().media(), medias, notas.totalReviewsAtivas(),
                totalConclusoes, totalEmWishlists, totalNegociacoes, agora);
    }

    /** Ficha de um jogo que ainda não teve nenhum evento. */
    public static EstatisticaJogo semDados(UUID jogoId) {
        return consolidar(jogoId, NotasDoJogo.vazias(), 0, 0, 0, null);
    }

    public boolean elegivelParaExibicao() {
        return totalReviewsAtivas >= MINIMO_DE_REVIEWS_PARA_EXIBICAO;
    }
}
