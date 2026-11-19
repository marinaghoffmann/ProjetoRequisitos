package com.g4.game_tracker_backend.dominio.moderacao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;

/** Só acrescenta: não há operação de alteração nem de exclusão de logs. */
public interface LogModeracaoRepositorio {
    LogDeModeracao registrar(LogDeModeracao log);
    Optional<LogDeModeracao> buscarPorId(UUID id);
    List<LogDeModeracao> listarPorConteudo(UUID conteudoId);
    /** Histórico do mais recente para o mais antigo. Página a partir de 1. */
    Pagina<LogDeModeracao> historico(int pagina, int tamanho);
}
