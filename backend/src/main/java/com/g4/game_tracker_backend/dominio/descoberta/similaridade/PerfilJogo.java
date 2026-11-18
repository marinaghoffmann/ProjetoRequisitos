package com.g4.game_tracker_backend.dominio.descoberta.similaridade;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * O que a engine de similaridade sabe de um jogo, traduzido do catálogo pela PerfilJogoGateway (ACL):
 * gêneros, tags (descritores livres, em minúsculas) e estúdio (desenvolvedora).
 */
public record PerfilJogo(UUID jogoId, Set<String> generos, Set<String> tags, UUID desenvolvedoraId) {

    public PerfilJogo {
        Objects.requireNonNull(jogoId, "O jogo do perfil é obrigatório");
        generos = Set.copyOf(generos == null ? Set.of() : generos);
        tags = Set.copyOf(tags == null ? Set.of() : tags);
    }
}
