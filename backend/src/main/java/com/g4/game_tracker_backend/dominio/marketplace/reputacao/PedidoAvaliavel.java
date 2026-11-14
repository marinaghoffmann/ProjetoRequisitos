package com.g4.game_tracker_backend.dominio.marketplace.reputacao;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * O que a F-21 precisa saber de um pedido da F-20 para aceitar uma avaliação: as partes, se ele foi
 * concluído e quando a entrega foi confirmada (início do prazo de 15 dias).
 */
public record PedidoAvaliavel(UUID pedidoId, UUID compradorId, UUID vendedorId, boolean concluido,
        Instant entregaConfirmadaEm) {

    public PedidoAvaliavel {
        Objects.requireNonNull(pedidoId, "O pedido é obrigatório");
        Objects.requireNonNull(compradorId, "O comprador é obrigatório");
        Objects.requireNonNull(vendedorId, "O vendedor é obrigatório");
        if (concluido && entregaConfirmadaEm == null) {
            throw new IllegalArgumentException("Pedido concluído precisa da data de confirmação da entrega");
        }
    }
}
