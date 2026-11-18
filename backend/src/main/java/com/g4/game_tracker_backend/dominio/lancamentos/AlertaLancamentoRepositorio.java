package com.g4.game_tracker_backend.dominio.lancamentos;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface AlertaLancamentoRepositorio {
    NotificacaoLancamento salvar(NotificacaoLancamento alerta);
    Optional<NotificacaoLancamento> buscar(UUID id);
    Optional<NotificacaoLancamento> pendentePorInteresse(UUID interesseId);
    List<NotificacaoLancamento> vencidos(Instant agora);
    void cancelarPendentes(UUID interesseId);
    void excluirPorInteresse(UUID interesseId);
}
