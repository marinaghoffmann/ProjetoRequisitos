package com.g4.game_tracker_backend.aplicacao.wishlist;

import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.IdentidadeAutenticada;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.MetricasDoJogoAlteradas;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;
import com.g4.game_tracker_backend.dominio.compartilhado.PublicadorDeEventos;

/** Remove o jogo da wishlist e, se ele estava lá, avisa a F-17 para recalcular a ficha. */
@CasoDeUso
public class RemoverDaWishlist {

    private final IdentidadeAutenticada identidade;
    private final WishlistRepositorio wishlists;
    private final PublicadorDeEventos eventos;

    public RemoverDaWishlist(IdentidadeAutenticada identidade, WishlistRepositorio wishlists,
            PublicadorDeEventos eventos) {
        this.identidade = identidade;
        this.wishlists = wishlists;
        this.eventos = eventos;
    }

    @Transacional
    public void executar(UUID jogoId) {
        wishlists.buscarPorDono(identidade.usuarioId())
                .filter(atual -> atual.contem(jogoId))
                .ifPresent(atual -> {
                    wishlists.salvar(atual.remover(jogoId));
                    // F-17 RN 1: só recalcula se o jogo de fato saiu da wishlist.
                    eventos.publicar(new MetricasDoJogoAlteradas(jogoId));
                });
    }
}