package com.g4.game_tracker_backend.aplicacao.identidade;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.identidade.Papel;

/** Porta para consultar a identidade verificada da requisição atual. */
public interface IdentidadeAutenticada {

    UUID usuarioId();

    Papel papel();
}
