package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.time.LocalDate;
import java.util.List;

public interface ImportadorJogoExterno {
    DadosJogoExterno buscar(String identificador);

    record DadosJogoExterno(String identificador, String titulo, String tituloOriginal, String resumo,
            LocalDate dataLancamento, String capaUrl, List<String> desenvolvedoras, List<String> generos) {
    }
}
