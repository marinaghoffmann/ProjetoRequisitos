package com.g4.game_tracker_backend.apresentacao.catalogo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DesenvolvedoraRequest(
        @NotBlank @Size(max = 160) String nome,
        @Size(max = 2000) String biografia,
        @Size(max = 500) String site,
        @Size(max = 1000) String redesSociais,
        @Size(max = 120) String pais) {
}
