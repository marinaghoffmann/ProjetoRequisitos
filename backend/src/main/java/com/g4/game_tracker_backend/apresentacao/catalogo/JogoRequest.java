package com.g4.game_tracker_backend.apresentacao.catalogo;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * No cadastro manual a desenvolvedora vem do vínculo do usuário e desenvolvedoraId é ignorado;
 * na edição, quando omitido, mantém a desenvolvedora atual.
 */
public record JogoRequest(
        @NotBlank @Size(max = 180) String titulo,
        @Size(max = 180) String tituloOriginal,
        @Size(max = 600) String resumo,
        UUID desenvolvedoraId,
        Set<UUID> generosIds,
        LocalDate dataLancamento,
        @Size(max = 1000) String capaUrl,
        @Size(max = 1000) String trailerUrl,
        Map<String, Object> metadados) {
}
