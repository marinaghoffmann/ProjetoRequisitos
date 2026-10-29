package com.g4.game_tracker_backend.apresentacao.marketplace.anuncios;

import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;

import jakarta.validation.constraints.NotNull;

public record AlterarStatusAnuncioRequest(@NotNull StatusAnuncio status) {
}
