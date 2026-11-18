package com.g4.game_tracker_backend.aplicacao.wishlist;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.IdentidadeAutenticada;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.MetricasDoJogoAlteradas;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;

/** Adiciona o jogo à wishlist e, se ele ainda não estava lá, avisa a F-17 para recalcular a ficha. */
@Service
public class AdicionarNaWishlist {

    private final IdentidadeAutenticada identidade;
    private final WishlistRepositorio wishlist;
    private final JogoRepositorio jogos;
    private final ApplicationEventPublisher eventos;

    public AdicionarNaWishlist(IdentidadeAutenticada identidade, WishlistRepositorio wishlist,
            JogoRepositorio jogos, ApplicationEventPublisher eventos) {
        this.identidade = identidade;
        this.wishlist = wishlist;
        this.jogos = jogos;
        this.eventos = eventos;
    }

    @Transactional
    public void executar(UUID jogoId) {
        if (jogos.buscarPorId(jogoId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Jogo não encontrado");
        }
        UUID usuarioId = identidade.usuarioId();
        if (wishlist.contem(usuarioId, jogoId)) {
            return;
        }
        wishlist.adicionar(usuarioId, jogoId);
        eventos.publishEvent(new MetricasDoJogoAlteradas(jogoId));
    }
}
