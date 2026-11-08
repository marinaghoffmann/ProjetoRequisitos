package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.List;

public interface BuscadorDeCapturas {

    record Captura(String miniatura, String completa) {
    }

    List<Captura> buscar(String appId);
}