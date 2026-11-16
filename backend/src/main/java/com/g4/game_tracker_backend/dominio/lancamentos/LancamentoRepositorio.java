package com.g4.game_tracker_backend.dominio.lancamentos;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface LancamentoRepositorio {
    LancamentoFuturo salvar(LancamentoFuturo lancamento);
    Optional<LancamentoFuturo> buscar(UUID id);
    Optional<LancamentoFuturo> buscarPorJogo(UUID jogoId);
    List<LancamentoFuturo> listarFuturos();
    List<LancamentoFuturo> listarVencidos(LocalDate hoje);
    void excluir(UUID id);
}
