package com.g4.game_tracker_backend.aplicacao.avaliacao.reviews;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.MetricasDoJogoAlteradas;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.NotaInformada;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.Review;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ReviewJaPublicadaException;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ReviewRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.journal.eventos.ReviewPublicada;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;
import com.g4.game_tracker_backend.dominio.compartilhado.PublicadorDeEventos;

/**
 * F-02: o usuário autenticado publica a review de um jogo ATIVO do catálogo.
 *
 * RN 1: se ele já avaliou o jogo, nada é criado e a ReviewJaPublicadaException leva o id da review
 * existente, para o redirecionamento à edição.
 *
 * Dentro da transação publica ReviewPublicada (F-16, entrada automática do journal, que sofre rollback
 * junto) e MetricasDoJogoAlteradas (F-17, recalculada depois do commit).
 */
@CasoDeUso
public class PublicarReview {

    private final ReviewRepositorio reviews;
    private final JogoRepositorio jogos;
    private final VerificadorDeAcesso verificadorDeAcesso;
    private final PublicadorDeEventos eventos;

    public PublicarReview(ReviewRepositorio reviews, JogoRepositorio jogos, VerificadorDeAcesso verificadorDeAcesso,
            PublicadorDeEventos eventos) {
        this.reviews = reviews;
        this.jogos = jogos;
        this.verificadorDeAcesso = verificadorDeAcesso;
        this.eventos = eventos;
    }

    @Transacional
    public Review executar(UUID jogoId, Integer notaGeral, List<NotaInformada> notasPorCategoria, String texto) {
        UUID autorId = verificadorDeAcesso.usuarioAtualId();
        JogosAvaliaveis.exigirAtivo(jogos, jogoId);
        reviews.buscarPorAutorEJogo(autorId, jogoId).ifPresent(existente -> {
            throw new ReviewJaPublicadaException(existente.id());
        });

        Review review = reviews.salvar(Review.publicar(autorId, jogoId, notaGeral, notasPorCategoria, texto,
                Instant.now()));
        eventos.publicar(new ReviewPublicada(autorId, jogoId, review.id().toString(), review.criadaEm()));
        eventos.publicar(new MetricasDoJogoAlteradas(jogoId));
        return review;
    }
}
