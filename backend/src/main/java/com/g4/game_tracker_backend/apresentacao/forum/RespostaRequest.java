package com.g4.game_tracker_backend.apresentacao.forum;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Size;

/** respostaPaiId nulo responde direto ao tópico; na edição, respostaPaiId e imagens são ignorados. */
public record RespostaRequest(UUID respostaPaiId, @Size(max = 5000) String conteudo,
        @Size(max = 4) List<String> imagens) {
}