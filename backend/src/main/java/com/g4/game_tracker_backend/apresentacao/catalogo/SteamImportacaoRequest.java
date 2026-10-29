package com.g4.game_tracker_backend.apresentacao.catalogo;

import jakarta.validation.constraints.NotBlank;

public record SteamImportacaoRequest(@NotBlank String termo) {
}
