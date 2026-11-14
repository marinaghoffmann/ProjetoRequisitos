package com.g4.game_tracker_backend.dominio.marketplace.pedidos;

/**
 * Status da entrega. O pedido nasce já pago, aguardando o envio:
 *
 * <pre>
 * AGUARDANDO_ENVIO -> ENVIADO (RN 8) -> CONCLUIDO (RN 7 e RN 10)
 * AGUARDANDO_ENVIO -> CANCELADO (prazo de envio vencido, RN 9)
 * </pre>
 */
public enum StatusPedido {
    AGUARDANDO_ENVIO,
    ENVIADO,
    CONCLUIDO,
    CANCELADO
}
