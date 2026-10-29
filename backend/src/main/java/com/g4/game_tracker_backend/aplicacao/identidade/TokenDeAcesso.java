package com.g4.game_tracker_backend.aplicacao.identidade;

import java.time.Instant;

public record TokenDeAcesso(String valor, Instant expiraEm) {
}
