package com.g4.game_tracker_backend.apresentacao.identidade;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String senha) {
}
