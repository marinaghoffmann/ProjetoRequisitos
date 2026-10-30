package com.g4.game_tracker_backend.apresentacao.forum;

import java.util.UUID;

import jakarta.validation.constraints.Size;

/** respostaPaiId nulo responde direto ao tópico; na edição ele é ignorado. */
public record RespostaRequest(UUID respostaPaiId, @Size(max = 5000) String conteudo) {
}
