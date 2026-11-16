package com.g4.game_tracker_backend.aplicacao.avaliacao.reviews;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.avaliacao.reviews.Review;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ReviewRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/** Buscas compartilhadas pelos casos de uso da F-02. */
final class JogosAvaliaveis {

    private JogosAvaliaveis() {
    }

    /** Só se publica review de um jogo que existe e está ATIVO no catálogo. */
    static Jogo exigirAtivo(JogoRepositorio jogos, UUID jogoId) {
        if (jogoId == null) {
            throw new RegraDeNegocioException("O jogo é obrigatório");
        }
        Jogo jogo = exigirExistente(jogos, jogoId);
        if (jogo.status() != StatusCatalogo.ATIVO) {
            throw new RegraDeNegocioException("O jogo precisa estar ativo no catálogo");
        }
        return jogo;
    }

    static Jogo exigirExistente(JogoRepositorio jogos, UUID jogoId) {
        return jogos.buscarPorId(jogoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
    }

    static Review review(ReviewRepositorio reviews, UUID reviewId) {
        return reviews.buscarPorId(reviewId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Review não encontrada"));
    }
}
