package com.g4.game_tracker_backend.infraestrutura.persistencia.reviews;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.FonteDeReviews;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.NotasAgregadas;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.NotasDoJogo;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.CategoriaReview;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ResumoDeNotas;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ReviewRepositorio;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.SomaDeNotas;

/**
 * Liga a F-02 à F-17: entrega à engine de estatísticas as notas geral e por categoria das reviews do jogo.
 * Como a exclusão da review é física, toda review gravada conta como ativa.
 */
@Component
public class ReviewsParaEstatisticas implements FonteDeReviews {

    private final ReviewRepositorio reviews;

    public ReviewsParaEstatisticas(ReviewRepositorio reviews) {
        this.reviews = reviews;
    }

    @Override
    public NotasDoJogo notasDoJogo(UUID jogoId) {
        ResumoDeNotas notas = reviews.resumirNotas(jogoId);
        return new NotasDoJogo(
                agregadas(notas.geral()),
                agregadas(notas.daCategoria(CategoriaReview.GRAFICOS)),
                agregadas(notas.daCategoria(CategoriaReview.SOM)),
                agregadas(notas.daCategoria(CategoriaReview.JOGABILIDADE)),
                agregadas(notas.daCategoria(CategoriaReview.DIVERSAO)),
                agregadas(notas.daCategoria(CategoriaReview.INOVACAO)));
    }

    private static NotasAgregadas agregadas(SomaDeNotas soma) {
        return new NotasAgregadas(BigDecimal.valueOf(soma.soma()), soma.quantidade());
    }
}
