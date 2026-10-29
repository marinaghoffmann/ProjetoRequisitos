package com.g4.game_tracker_backend.apresentacao.catalogo;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ImportarJogoRequest(
        @NotBlank @Size(max = 180) String titulo,
        @Size(max = 180) String tituloOriginal,
        @Size(max = 600) String resumo,
        @NotBlank @Size(max = 160) String identificadorExterno,
        @NotNull UUID desenvolvedoraId,
        Set<UUID> generosIds,
        LocalDate dataLancamento,
        @Size(max = 1000) String capaUrl,
        @Size(max = 1000) String trailerUrl) {
}
