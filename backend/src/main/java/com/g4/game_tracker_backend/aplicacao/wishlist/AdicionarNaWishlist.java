package com.g4.game_tracker_backend.aplicacao.wishlist;

import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.IdentidadeAutenticada;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.MetricasDoJogoAlteradas;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.wishlist.PrioridadeWishlist;
import com.g4.game_tracker_backend.dominio.wishlist.Wishlist;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;

/**
 * Adiciona o jogo à wishlist do usuário, criando-a na primeira adição (RN 1), e avisa a F-17 para
 * recalcular a ficha do jogo.
 */
@Service
public class AdicionarNaWishlist {

    private final IdentidadeAutenticada identidade;
    private final WishlistRepositorio wishlists;
    private final JogoRepositorio jogos;
    private final ApplicationEventPublisher eventos;

    public AdicionarNaWishlist(IdentidadeAutenticada identidade, WishlistRepositorio wishlists,
            JogoRepositorio jogos, ApplicationEventPublisher eventos) {
        this.identidade = identidade;
        this.wishlists = wishlists;
        this.jogos = jogos;
        this.eventos = eventos;
    }

    @Transactional
    public void executar(UUID jogoId, PrioridadeWishlist prioridade) {
        Jogo jogo = jogos.buscarPorId(jogoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
        if (jogo.status() != StatusCatalogo.ATIVO) {
            throw new RegraDeNegocioException("Este jogo não está disponível no catálogo");
        }
        UUID donoId = identidade.usuarioId();
        Instant agora = Instant.now();
        Wishlist atual = wishlists.buscarPorDono(donoId).orElseGet(() -> Wishlist.criar(donoId, agora));
        wishlists.salvar(atual.adicionar(jogoId, prioridade, agora));
        // F-17 RN 1: a ficha do jogo recalcula a presença em wishlists depois do commit.
        eventos.publishEvent(new MetricasDoJogoAlteradas(jogoId));
    }
}