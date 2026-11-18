package com.g4.game_tracker_backend.aplicacao.wishlist;

import java.time.Instant;

import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.wishlist.PrioridadeWishlist;

public record ItemWishlistDetalhado(Jogo jogo, PrioridadeWishlist prioridade, Instant adicionadoEm) {
}