package com.g4.game_tracker_backend.apresentacao.catalogo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GeneroRequest(@NotBlank @Size(max = 80) String nome) {
}
