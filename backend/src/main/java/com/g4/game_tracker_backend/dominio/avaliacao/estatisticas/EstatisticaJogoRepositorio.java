package com.g4.game_tracker_backend.dominio.avaliacao.estatisticas;

import java.util.Optional;
import java.util.UUID;

public interface EstatisticaJogoRepositorio {
    EstatisticaJogo salvar(EstatisticaJogo estatistica);
    Optional<EstatisticaJogo> buscarPorJogo(UUID jogoId);
}
