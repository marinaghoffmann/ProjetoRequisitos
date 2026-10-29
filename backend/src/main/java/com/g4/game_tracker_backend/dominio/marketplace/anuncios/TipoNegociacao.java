package com.g4.game_tracker_backend.dominio.marketplace.anuncios;

/** Modalidade aceita no anúncio (F-18). */
public enum TipoNegociacao {
    VENDA,
    TROCA,
    AMBOS;

    public boolean aceitaVenda() {
        return this != TROCA;
    }

    public boolean aceitaTroca() {
        return this != VENDA;
    }
}
