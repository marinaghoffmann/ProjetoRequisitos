package com.g4.game_tracker_backend.dominio.avaliacao.estatisticas;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EstatisticaJogoRepositorio {
    EstatisticaJogo salvar(EstatisticaJogo estatistica);
    Optional<EstatisticaJogo> buscarPorJogo(UUID jogoId);
    /** Todas as fichas já consolidadas (jogos sem nenhum evento ainda não têm ficha). */
    List<EstatisticaJogo> listarTodas();
}
