package com.g4.game_tracker_backend.apresentacao.wishlist;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.catalogo.Jogo;

public record ItemWishlistResponse(UUID jogoId, String titulo, String capaUrl) {

    static ItemWishlistResponse de(Jogo jogo) {
        return new ItemWishlistResponse(jogo.id(), jogo.titulo(), jogo.capaUrl());
    }
}