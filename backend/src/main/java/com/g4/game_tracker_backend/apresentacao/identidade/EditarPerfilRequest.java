package com.g4.game_tracker_backend.apresentacao.identidade;

import jakarta.validation.constraints.Size;

/** Dados editáveis do próprio perfil. As regras (nome obrigatório, bio e localização vazias viram nulas) ficam no domínio. */
public record EditarPerfilRequest(
        @Size(max = 80) String nome,
        @Size(max = 160) String bio,
        @Size(max = 80) String localizacao) {
}