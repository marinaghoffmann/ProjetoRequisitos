package com.g4.game_tracker_backend.aplicacao.wishlist;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.IdentidadeAutenticada;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;

@Service
public class RemoverDaWishlist {

    private final IdentidadeAutenticada identidade;
    private final WishlistRepositorio wishlist;

    public RemoverDaWishlist(IdentidadeAutenticada identidade, WishlistRepositorio wishlist) {
        this.identidade = identidade;
        this.wishlist = wishlist;
    }

    @Transactional
    public void executar(UUID jogoId) {
        wishlist.remover(identidade.usuarioId(), jogoId);
    }
}