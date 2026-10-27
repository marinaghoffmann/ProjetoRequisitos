package com.g4.game_tracker_backend.dominio.catalogo;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface GeneroRepositorio {
    Genero salvar(Genero genero);
    Optional<Genero> buscarPorId(UUID id);
    Optional<Genero> buscarPorNome(String nome);
    List<Genero> listar();
}
