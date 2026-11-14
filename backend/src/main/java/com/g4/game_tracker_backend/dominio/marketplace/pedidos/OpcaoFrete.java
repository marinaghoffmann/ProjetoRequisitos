package com.g4.game_tracker_backend.dominio.marketplace.pedidos;

import java.math.BigDecimal;

/** F-20 RN 4: as duas opções de frete simuladas e os seus valores. */
public enum OpcaoFrete {
    ENVIO_SEGURO(new BigDecimal("18.90")),
    EXPRESSO(new BigDecimal("32.90"));

    private final BigDecimal valor;

    OpcaoFrete(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal valor() {
        return valor;
    }
}
