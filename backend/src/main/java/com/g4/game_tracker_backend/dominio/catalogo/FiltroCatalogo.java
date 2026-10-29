package com.g4.game_tracker_backend.dominio.catalogo;

import java.util.UUID;

public record FiltroCatalogo(String titulo, UUID generoId, UUID desenvolvedoraId,
        OrigemJogo origem, StatusCatalogo status) {

    public static FiltroCatalogo ativos() {
        return new FiltroCatalogo(null, null, null, null, StatusCatalogo.ATIVO);
    }
}
