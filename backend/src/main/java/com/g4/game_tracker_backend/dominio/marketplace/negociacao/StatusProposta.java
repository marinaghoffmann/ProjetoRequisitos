package com.g4.game_tracker_backend.dominio.marketplace.negociacao;

/**
 * F-19 RN 8 e RN 10. Toda proposta nasce PENDENTE. Recusada, cancelada e expirada são finais; a
 * aceita só pode expirar (prazo de 48 horas sem pedido ou reserva liberada pelo vendedor).
 */
public enum StatusProposta {
    PENDENTE,
    ACEITA,
    RECUSADA,
    CANCELADA,
    EXPIRADA
}
