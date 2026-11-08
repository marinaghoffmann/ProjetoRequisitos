package com.g4.game_tracker_backend.apresentacao.identidade;

import com.g4.game_tracker_backend.dominio.identidade.Papel;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CadastroPrivilegiadoRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 8) String senha,
        @NotNull Papel papel) {
}