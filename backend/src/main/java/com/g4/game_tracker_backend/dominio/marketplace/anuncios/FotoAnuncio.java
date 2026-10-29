package com.g4.game_tracker_backend.dominio.marketplace.anuncios;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/** Foto real do item anunciado (F-18 RN 1), na ordem em que é exibida. */
public record FotoAnuncio(String urlFoto, int ordem) {

    public FotoAnuncio {
        if (urlFoto == null || urlFoto.isBlank()) {
            throw new RegraDeNegocioException("A URL da foto é obrigatória");
        }
        if (urlFoto.length() > 1000) {
            throw new RegraDeNegocioException("A URL da foto deve ter no máximo 1000 caracteres");
        }
        urlFoto = urlFoto.trim();
    }
}
