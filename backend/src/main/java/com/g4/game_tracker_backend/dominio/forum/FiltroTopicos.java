package com.g4.game_tracker_backend.dominio.forum;

import java.util.UUID;

/** Filtros da consulta de tópicos (F-13): por jogo, por categoria, ou ambos. Os dois são opcionais. */
public record FiltroTopicos(UUID jogoId, String categoria) {

    public static FiltroTopicos todos() {
        return new FiltroTopicos(null, null);
    }
}
