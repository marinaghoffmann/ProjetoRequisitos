package com.g4.game_tracker_backend.dominio.lancamentos;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface InteresseLancamentoRepositorio {
    InteresseEmLancamento salvar(InteresseEmLancamento interesse);
    Optional<InteresseEmLancamento> buscar(UUID usuarioId, UUID jogoId);
    Optional<InteresseEmLancamento> buscarPorId(UUID id);
    List<InteresseEmLancamento> ativosPorJogo(UUID jogoId);
    long contarAtivosPorJogo(UUID jogoId);
    List<InteresseEmLancamento> todosPorJogo(UUID jogoId);
    void excluirPorJogo(UUID jogoId);
}
