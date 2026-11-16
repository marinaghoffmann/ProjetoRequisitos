package com.g4.game_tracker_backend.apresentacao.avaliacao.reviews;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.avaliacao.reviews.ConsultarReviews.JogoAvaliado;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.Review;

/**
 * Review com o nome do autor e, nas listas de reviews de vários jogos (perfil), o título e a capa do jogo.
 * editada indica que a review mudou depois da publicação.
 */
public record ReviewResponse(UUID id, UUID autorId, String nomeAutor, UUID jogoId, String jogoTitulo,
        String jogoCapaUrl, int notaGeral, List<Nota> notasPorCategoria, String texto, Instant criadaEm,
        Instant atualizadaEm, boolean editada) {

    public record Nota(String categoria, int nota) {
    }

    static ReviewResponse de(Review review, Map<UUID, String> nomes, Map<UUID, JogoAvaliado> jogos) {
        JogoAvaliado jogo = jogos.get(review.jogoId());
        return new ReviewResponse(review.id(), review.autorId(), nomes.get(review.autorId()), review.jogoId(),
                jogo == null ? null : jogo.titulo(), jogo == null ? null : jogo.capaUrl(), review.notaGeral().valor(),
                review.notasPorCategoria().stream()
                        .map(nota -> new Nota(nota.categoria().name(), nota.nota().valor()))
                        .toList(),
                review.texto(), review.criadaEm(), review.atualizadaEm(),
                review.atualizadaEm().isAfter(review.criadaEm()));
    }
}
