package com.g4.game_tracker_backend.dominio.forum;

import java.util.Optional;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;

public interface TopicoDeForumRepositorio {
    TopicoDeForum salvar(TopicoDeForum topico);
    Optional<TopicoDeForum> buscarPorId(UUID id);
    /** Bloqueia a linha do tópico até o fim da transação antes de alterar respostas ou metadados. */
    Optional<TopicoDeForum> buscarPorIdParaAlteracao(UUID id);
    /** Tópicos não removidos, do mais recentemente ativo para o menos. A página começa em 1. */
    Pagina<ResumoTopico> listar(FiltroTopicos filtro, int pagina, int tamanho);
}
