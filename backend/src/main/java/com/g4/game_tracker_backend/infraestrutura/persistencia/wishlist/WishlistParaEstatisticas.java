package com.g4.game_tracker_backend.infraestrutura.persistencia.wishlist;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.FonteDeWishlists;
import com.g4.game_tracker_backend.dominio.wishlist.WishlistRepositorio;

/** Liga a wishlist (F-06) à ficha de estatísticas (F-17). */
@Component
class WishlistParaEstatisticas implements FonteDeWishlists {

    private final WishlistRepositorio wishlist;

    WishlistParaEstatisticas(WishlistRepositorio wishlist) {
        this.wishlist = wishlist;
    }

    @Override
    public int contarPresencaEmWishlists(UUID jogoId) {
        return wishlist.contarPorJogo(jogoId);
    }
}
