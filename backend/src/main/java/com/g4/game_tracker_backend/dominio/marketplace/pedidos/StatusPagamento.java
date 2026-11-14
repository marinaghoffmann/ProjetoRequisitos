package com.g4.game_tracker_backend.dominio.marketplace.pedidos;

/** F-20 RN 7: o valor fica EM_CUSTODIA até ser LIBERADO ao vendedor ou REEMBOLSADO ao comprador. */
public enum StatusPagamento {
    EM_CUSTODIA,
    LIBERADO,
    REEMBOLSADO
}
