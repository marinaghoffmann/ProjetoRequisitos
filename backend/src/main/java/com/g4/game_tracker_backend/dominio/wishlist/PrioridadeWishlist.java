package com.g4.game_tracker_backend.dominio.wishlist;

/** A ordem de declaração define a ordem de listagem: ALTA, MEDIA, BAIXA. */
public enum PrioridadeWishlist {
    ALTA, MEDIA, BAIXA;

    public static final PrioridadeWishlist PADRAO = MEDIA; // RN 3
}