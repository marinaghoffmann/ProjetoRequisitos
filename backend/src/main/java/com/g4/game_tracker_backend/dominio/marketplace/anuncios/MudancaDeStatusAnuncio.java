package com.g4.game_tracker_backend.dominio.marketplace.anuncios;

/** Resultado de uma transição: o anúncio já no novo status e o evento que deve ser publicado. */
public record MudancaDeStatusAnuncio(AnuncioMarketplace anuncio, AnuncioStatusAlterado evento) {
}
