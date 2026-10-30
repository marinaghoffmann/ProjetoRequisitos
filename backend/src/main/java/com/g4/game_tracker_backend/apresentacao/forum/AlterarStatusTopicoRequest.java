package com.g4.game_tracker_backend.apresentacao.forum;

import com.g4.game_tracker_backend.dominio.forum.StatusTopico;

import jakarta.validation.constraints.NotNull;

public record AlterarStatusTopicoRequest(@NotNull StatusTopico status) {
}
