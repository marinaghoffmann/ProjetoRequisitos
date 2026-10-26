package com.g4.game_tracker_backend.apresentacao.identidade;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;

public record UsuarioResponse(UUID id, String nome, String email, Papel papel) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.id(), usuario.nome(), usuario.email(), usuario.papel());
    }
}
