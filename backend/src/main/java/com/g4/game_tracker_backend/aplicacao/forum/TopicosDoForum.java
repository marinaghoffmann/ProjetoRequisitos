package com.g4.game_tracker_backend.aplicacao.forum;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForumRepositorio;

/** Busca um tópico visível: tópicos removidos se comportam como inexistentes. */
final class TopicosDoForum {

    private TopicosDoForum() {
    }

    static TopicoDeForum visivel(TopicoDeForumRepositorio topicos, UUID id) {
        return topicos.buscarPorId(id)
                .filter(topico -> !topico.removido())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tópico não encontrado"));
    }
}
