package com.g4.game_tracker_backend.apresentacao.identidade;

import com.g4.game_tracker_backend.dominio.identidade.Papel;

import jakarta.validation.constraints.NotNull;

public record AlterarPapelUsuarioRequest(@NotNull Papel papel) {
}
