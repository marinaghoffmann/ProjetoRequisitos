package com.g4.game_tracker_backend.aplicacao.wishlist;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.IdentidadeAutenticada;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.MetricasDoJogoAlteradas;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;

@Service
public class RemoverDaWishlist {

    private final IdentidadeAutenticada identidade;
    private final WishlistRepositorio wishlists;
    private final ApplicationEventPublisher eventos;

    public RemoverDaWishlist(IdentidadeAutenticada identidade, WishlistRepositorio wishlists,
            ApplicationEventPublisher eventos) {
        this.identidade = identidade;
        this.wishlists = wishlists;
        this.eventos = eventos;
    }

    @Transactional
    public void executar(UUID jogoId) {
        wishlists.buscarPorDono(identidade.usuarioId())
                .filter(atual -> atual.contem(jogoId))
                .ifPresent(atual -> {
                    wishlists.salvar(atual.remover(jogoId));
                    // F-17 RN 1: só recalcula se o jogo de fato saiu da wishlist.
                    eventos.publishEvent(new MetricasDoJogoAlteradas(jogoId));
                });
    }
}