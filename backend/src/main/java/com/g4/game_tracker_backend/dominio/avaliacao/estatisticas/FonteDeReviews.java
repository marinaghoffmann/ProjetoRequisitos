package com.g4.game_tracker_backend.dominio.avaliacao.estatisticas;

import java.util.UUID;

/** Porta para os dados da F-02. Enquanto não houver implementação, o jogo fica sem notas. */
public interface FonteDeReviews {
    NotasDoJogo notasDoJogo(UUID jogoId);
}
