package com.g4.game_tracker_backend.apresentacao.wishlist;

import com.g4.game_tracker_backend.dominio.wishlist.PrioridadeWishlist;

import jakarta.validation.constraints.NotNull;

public record AlterarPrioridadeWishlistRequest(@NotNull PrioridadeWishlist prioridade) {
}