package com.g4.game_tracker_backend.apresentacao.wishlist;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.wishlist.PrioridadeWishlist;

import jakarta.validation.constraints.NotNull;

public record AdicionarWishlistRequest(@NotNull UUID jogoId, PrioridadeWishlist prioridade) {
}