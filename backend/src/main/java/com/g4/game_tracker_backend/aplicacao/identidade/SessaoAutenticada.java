package com.g4.game_tracker_backend.aplicacao.identidade;

import com.g4.game_tracker_backend.dominio.identidade.Usuario;

public record SessaoAutenticada(Usuario usuario, TokenDeAcesso token) {
}
