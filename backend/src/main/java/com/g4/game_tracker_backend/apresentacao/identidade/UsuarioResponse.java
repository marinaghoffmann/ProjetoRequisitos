package com.g4.game_tracker_backend.apresentacao.identidade;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;

public record UsuarioResponse(UUID id, String nome, String email, Papel papel, UUID desenvolvedoraId, String bio,
        String localizacao, String avatarUrl, String bannerUrl) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.id(), usuario.nome(), usuario.email(), usuario.papel(),
                usuario.desenvolvedoraId(), usuario.bio(), usuario.localizacao(),
                usuario.avatarUrl(), usuario.bannerUrl());
    }
}