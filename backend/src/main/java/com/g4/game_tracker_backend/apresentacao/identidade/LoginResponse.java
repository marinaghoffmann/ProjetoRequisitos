package com.g4.game_tracker_backend.apresentacao.identidade;

import java.time.Instant;

public record LoginResponse(String accessToken, String tokenType, Instant expiresAt,
        UsuarioResponse usuario) {
}
