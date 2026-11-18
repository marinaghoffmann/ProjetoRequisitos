package com.g4.game_tracker_backend.apresentacao.wishlist;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.wishlist.ItemWishlistDetalhado;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.wishlist.PrioridadeWishlist;

public record ItemWishlistResponse(UUID jogoId, String titulo, String capaUrl,
        PrioridadeWishlist prioridade, Instant adicionadoEm) {

    static ItemWishlistResponse de(ItemWishlistDetalhado item) {
        return new ItemWishlistResponse(item.jogo().id(), item.jogo().titulo(), item.jogo().capaUrl(),
                item.prioridade(), item.adicionadoEm());
    }
}