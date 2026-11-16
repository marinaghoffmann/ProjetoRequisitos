package com.g4.game_tracker_backend.aplicacao.descoberta.similaridade;

import java.util.Optional;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeJogo;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.Papel;

/** Buscas e checagens compartilhadas pelos casos de uso da F-10. */
final class Sugestoes {

    static final String APENAS_ADMINISTRADORES = "Apenas administradores podem ajustar similaridades";

    private Sugestoes() {
    }

    /** RN 3: só o ADMINISTRADOR cria, remove ou recalcula conexões. */
    static void exigirAdministrador(VerificadorDeAcesso verificador) {
        if (!verificador.usuarioAtualTemAlgumPapel(Papel.ADMINISTRADOR)) {
            throw new AcessoNegadoException(APENAS_ADMINISTRADORES);
        }
    }

    /** Sugestão que aparece para os jogadores; removidas pelo administrador se comportam como inexistentes. */
    static SimilaridadeJogo visivel(SimilaridadeRepositorio similaridades, UUID similaridadeId) {
        return similaridades.buscarPorId(similaridadeId)
                .filter(SimilaridadeJogo::visivel)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sugestão não encontrada"));
    }

    /** A consulta de similares é pública: sem login, não há feedback do usuário para mostrar. */
    static Optional<UUID> usuarioAtualSeHouver(VerificadorDeAcesso verificador) {
        try {
            return Optional.of(verificador.usuarioAtualId());
        } catch (AcessoNegadoException naoAutenticado) {
            return Optional.empty();
        }
    }
}
