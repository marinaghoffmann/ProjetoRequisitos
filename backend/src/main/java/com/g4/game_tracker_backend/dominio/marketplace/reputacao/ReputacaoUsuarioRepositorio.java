package com.g4.game_tracker_backend.dominio.marketplace.reputacao;

import java.util.Optional;
import java.util.UUID;

public interface ReputacaoUsuarioRepositorio {
    ReputacaoUsuario salvar(ReputacaoUsuario reputacao);
    Optional<ReputacaoUsuario> buscarPorUsuario(UUID usuarioId);
    /** Trava a linha até o fim da transação, para o recálculo não perder avaliações simultâneas. */
    Optional<ReputacaoUsuario> buscarPorUsuarioParaAtualizar(UUID usuarioId);
}
