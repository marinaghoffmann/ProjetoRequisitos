package com.g4.game_tracker_backend.apresentacao.forum;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Size;

/**
 * Dados do tópico. jogoId nulo cria um tópico geral; categoria vazia vira "Geral".
 * As regras (título e conteúdo obrigatórios) ficam no domínio, para a mensagem chegar ao usuário.
 * imagens são URLs devolvidas por POST /api/forum/imagens; na edição, nulo mantém as atuais.
 */
public record TopicoRequest(
        UUID jogoId,
        @Size(max = 60) String categoria,
        @Size(max = 150) String titulo,
        @Size(max = 10000) String conteudo,
        @Size(max = 4) List<String> imagens) {
}