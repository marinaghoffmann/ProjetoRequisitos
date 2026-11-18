package com.g4.game_tracker_backend.infraestrutura.persistencia.wishlist;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.FonteDeWishlists;

@Component
public class FonteDeWishlistsPostgres implements FonteDeWishlists {

    private final WishlistSpringDataRepository springData;

    public FonteDeWishlistsPostgres(WishlistSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    @Transactional(readOnly = true)
    public int contarPresencaEmWishlists(UUID jogoId) {
        return (int) springData.contarComOJogo(jogoId);
    }
}