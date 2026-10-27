package com.g4.game_tracker_backend.apresentacao.catalogo;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.catalogo.Genero;

public record GeneroResponse(UUID id, String nome) {

    static GeneroResponse de(Genero genero) {
        return new GeneroResponse(genero.id(), genero.nome());
    }
}
