package com.g4.game_tracker_backend.apresentacao.identidade;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record VincularDesenvolvedoraRequest(@NotNull UUID desenvolvedoraId) {
}
