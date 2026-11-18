package com.g4.game_tracker_backend.dominio.forum;

import java.util.List;
import java.util.UUID;

/** Porta de persistência das curtidas e do contador usado na ordenação Em alta. */
public interface CurtidaForumRepositorio {
    boolean existe(UUID usuarioId, String tipo, UUID conteudoId);
    void adicionar(CurtidaForum curtida);
    void remover(UUID usuarioId, String tipo, UUID conteudoId);
    List<CurtidaForum> listar(UUID topicoId);
    void removerDaResposta(UUID respostaId);
    void sincronizarTotal(UUID topicoId);
}
