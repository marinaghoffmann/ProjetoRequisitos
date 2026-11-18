package com.g4.game_tracker_backend.apresentacao.wishlist;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record AdicionarWishlistRequest(@NotNull UUID jogoId) {
}