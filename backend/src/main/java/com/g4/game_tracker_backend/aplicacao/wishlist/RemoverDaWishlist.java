package com.g4.game_tracker_backend.aplicacao.wishlist;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.IdentidadeAutenticada;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;

@Service
public class RemoverDaWishlist {

    private final IdentidadeAutenticada identidade;
    private final WishlistRepositorio wishlists;

    public RemoverDaWishlist(IdentidadeAutenticada identidade, WishlistRepositorio wishlists) {
        this.identidade = identidade;
        this.wishlists = wishlists;
    }

    @Transactional
    public void executar(UUID jogoId) {
        wishlists.buscarPorDono(identidade.usuarioId())
                .ifPresent(atual -> wishlists.salvar(atual.remover(jogoId)));
    }
}