package com.g4.game_tracker_backend.dominio.wishlist;

import java.util.Optional;
import java.util.UUID;

public interface WishlistRepositorio {
    Optional<Wishlist> buscarPorDono(UUID donoId);
    Wishlist salvar(Wishlist wishlist);
}