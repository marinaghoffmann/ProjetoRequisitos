package com.g4.game_tracker_backend.bdd;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.g4.game_tracker_backend.dominio.identidade.Usuario;

/**
 * Simula, nos steps, a requisição autenticada que o filtro JWT montaria. O contexto é limpo
 * ao fim de cada cenário pelo hook em PassosUsuarioAutenticacao.
 */
final class SessaoDeTeste {

    private SessaoDeTeste() {
    }

    static void autenticarComo(Usuario usuario) {
        Jwt jwt = Jwt.withTokenValue("token-bdd")
                .header("alg", "none")
                .subject(usuario.id().toString())
                .claim("papel", usuario.papel().name())
                .build();
        // O construtor com authorities marca o token como autenticado, como faz o filtro real.
        var autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.papel().name()));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, autoridades));
    }
}
