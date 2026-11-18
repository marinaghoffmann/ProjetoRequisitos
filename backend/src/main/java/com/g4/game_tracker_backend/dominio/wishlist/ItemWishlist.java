package com.g4.game_tracker_backend.dominio.wishlist;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ItemWishlist(UUID jogoId, PrioridadeWishlist prioridade, Instant adicionadoEm) {

    public ItemWishlist {
        Objects.requireNonNull(jogoId, "O jogo é obrigatório");
        Objects.requireNonNull(adicionadoEm, "A data de adição é obrigatória");
        prioridade = prioridade == null ? PrioridadeWishlist.PADRAO : prioridade;
    }

    ItemWishlist comPrioridade(PrioridadeWishlist nova) {
        return new ItemWishlist(jogoId, nova, adicionadoEm);
    }
}