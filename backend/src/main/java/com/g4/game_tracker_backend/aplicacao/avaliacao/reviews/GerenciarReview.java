package com.g4.game_tracker_backend.aplicacao.avaliacao.reviews;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.MetricasDoJogoAlteradas;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.NotaInformada;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.Review;
import com.g4.game_tracker_backend.dominio.avaliacao.reviews.ReviewRepositorio;
import com.g4.game_tracker_backend.dominio.journal.eventos.ReviewExcluida;

/**
 * F-02 RN 2 e RN 3: o autor edita as notas e o texto, ou exclui a review de vez. O solicitante é sempre
 * o usuário autenticado, comparado pelo agregado com o autor.
 *
 * A exclusão é física e leva as notas por categoria junto. Ela publica ReviewExcluida (F-16 remove a
 * entrada automática) e, como a edição, MetricasDoJogoAlteradas (F-17).
 */
@Service
public class GerenciarReview {

    private final ReviewRepositorio reviews;
    private final VerificadorDeAcesso verificadorDeAcesso;
    private final ApplicationEventPublisher eventos;

    public GerenciarReview(ReviewRepositorio reviews, VerificadorDeAcesso verificadorDeAcesso,
            ApplicationEventPublisher eventos) {
        this.reviews = reviews;
        this.verificadorDeAcesso = verificadorDeAcesso;
        this.eventos = eventos;
    }

    @Transactional
    public Review editar(UUID reviewId, Integer notaGeral, List<NotaInformada> notasPorCategoria, String texto) {
        Review atual = JogosAvaliaveis.review(reviews, reviewId);
        Review editada = reviews.salvar(atual.editar(verificadorDeAcesso.usuarioAtualId(), notaGeral,
                notasPorCategoria, texto, Instant.now()));
        eventos.publishEvent(new MetricasDoJogoAlteradas(editada.jogoId()));
        return editada;
    }

    @Transactional
    public void excluir(UUID reviewId) {
        Review atual = JogosAvaliaveis.review(reviews, reviewId);
        atual.exigirAutor(verificadorDeAcesso.usuarioAtualId());
        reviews.remover(atual.id());
        eventos.publishEvent(new ReviewExcluida(atual.id().toString()));
        eventos.publishEvent(new MetricasDoJogoAlteradas(atual.jogoId()));
    }
}
