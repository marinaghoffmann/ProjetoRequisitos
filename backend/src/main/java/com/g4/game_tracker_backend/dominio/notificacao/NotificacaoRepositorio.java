package com.g4.game_tracker_backend.dominio.notificacao;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface NotificacaoRepositorio {
    Notificacao salvar(Notificacao notificacao);
    Optional<Notificacao> buscar(UUID id);
    List<Notificacao> listarPorUsuario(UUID usuarioId);
    boolean existePorOrigem(String origem);
}
