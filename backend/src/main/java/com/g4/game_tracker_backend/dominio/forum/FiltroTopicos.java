package com.g4.game_tracker_backend.dominio.forum;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/** Filtros da consulta de tópicos (F-13): por jogo, gerais sem jogo, ou categoria. */
public record FiltroTopicos(UUID jogoId, String categoria, boolean apenasGerais,
        StatusTopico status, boolean emAlta) {

    public FiltroTopicos {
        if (apenasGerais && jogoId != null) {
            throw new RegraDeNegocioException("Escolha um jogo ou apenas tópicos gerais");
        }
    }

    public FiltroTopicos(UUID jogoId, String categoria) {
        this(jogoId, categoria, false, null, false);
    }

    public FiltroTopicos(UUID jogoId, String categoria, boolean apenasGerais) {
        this(jogoId, categoria, apenasGerais, null, false);
    }

    public static FiltroTopicos todos() {
        return new FiltroTopicos(null, null, false, null, false);
    }
}
