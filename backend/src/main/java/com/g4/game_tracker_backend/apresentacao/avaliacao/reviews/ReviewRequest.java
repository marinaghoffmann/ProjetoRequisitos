package com.g4.game_tracker_backend.apresentacao.avaliacao.reviews;

import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.avaliacao.reviews.CategoriaReview;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.NotaInformada;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.Review;

import jakarta.validation.constraints.Size;

/**
 * Dados da review. Na edição, jogoId é ignorado: a review continua no mesmo jogo. O autor nunca vem daqui,
 * e sim do usuário autenticado. As regras (notas de 1 a 5, categoria única) ficam no domínio, para a
 * mensagem chegar ao usuário.
 */
public record ReviewRequest(
        UUID jogoId,
        Integer notaGeral,
        List<NotaCategoria> notasPorCategoria,
        @Size(max = Review.LIMITE_DO_TEXTO) String texto) {

    public record NotaCategoria(CategoriaReview categoria, Integer nota) {
    }

    List<NotaInformada> notasInformadas() {
        return notasPorCategoria == null
                ? List.of()
                : notasPorCategoria.stream()
                        .map(nota -> nota == null ? null : new NotaInformada(nota.categoria(), nota.nota()))
                        .toList();
    }
}
