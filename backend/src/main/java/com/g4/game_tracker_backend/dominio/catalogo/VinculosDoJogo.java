package com.g4.game_tracker_backend.dominio.catalogo;

import java.util.UUID;

/** Porta: diz se o jogo tem dados vinculados em outros módulos (reviews, listas, journal). */
public interface VinculosDoJogo {
    boolean possuiVinculos(UUID jogoId);
}