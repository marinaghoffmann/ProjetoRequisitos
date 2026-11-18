package com.g4.game_tracker_backend.dominio.wishlist;

import java.util.List;
import java.util.UUID;

public interface WishlistRepositorio {
    List<UUID> jogoIdsDoUsuario(UUID usuarioId);
    boolean contem(UUID usuarioId, UUID jogoId);
    void adicionar(UUID usuarioId, UUID jogoId);
    void remover(UUID usuarioId, UUID jogoId);
    int contarPorJogo(UUID jogoId);
}