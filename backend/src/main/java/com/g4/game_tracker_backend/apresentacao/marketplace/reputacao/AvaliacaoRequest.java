package com.g4.game_tracker_backend.apresentacao.marketplace.reputacao;

import java.util.Set;

import com.g4.game_tracker_backend.dominio.marketplace.reputacao.TagComportamento;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AvaliacaoRequest(
        @NotNull(message = "A nota é obrigatória")
        @Min(value = 1, message = "A nota deve ser de 1 a 5 estrelas")
        @Max(value = 5, message = "A nota deve ser de 1 a 5 estrelas")
        Integer estrelas,
        Set<TagComportamento> tags,
        String comentario) {
}
