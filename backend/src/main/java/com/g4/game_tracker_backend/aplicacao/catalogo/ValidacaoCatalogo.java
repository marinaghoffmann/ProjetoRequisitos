package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.Set;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.catalogo.GeneroRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;

/** Validações compartilhadas pelos casos de uso do catálogo. */
final class ValidacaoCatalogo {

    private ValidacaoCatalogo() {
    }

    static void exigirGeneros(GeneroRepositorio generos, Set<UUID> generosIds) {
        if (generosIds == null) {
            return;
        }
        for (UUID generoId : generosIds) {
            generos.buscarPorId(generoId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Gênero não encontrado: " + generoId));
        }
    }
}
