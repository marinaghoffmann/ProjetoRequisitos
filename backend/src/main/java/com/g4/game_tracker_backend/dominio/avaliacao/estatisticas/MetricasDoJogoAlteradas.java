package com.g4.game_tracker_backend.dominio.avaliacao.estatisticas;

import java.util.Objects;
import java.util.UUID;

/**
 * Pede o recálculo das estatísticas de um jogo (F-17 RN 1). Deve ser publicado ao criar, editar ou
 * excluir uma review (F-02), um registro de conclusão (F-11) ou um item de wishlist (F-06).
 */
public record MetricasDoJogoAlteradas(UUID jogoId) {

    public MetricasDoJogoAlteradas {
        Objects.requireNonNull(jogoId, "O jogo é obrigatório");
    }
}
