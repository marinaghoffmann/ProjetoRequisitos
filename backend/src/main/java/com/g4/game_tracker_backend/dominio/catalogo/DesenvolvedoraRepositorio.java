package com.g4.game_tracker_backend.dominio.catalogo;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface DesenvolvedoraRepositorio {
    Desenvolvedora salvar(Desenvolvedora desenvolvedora);
    Optional<Desenvolvedora> buscarPorId(UUID id);
    Optional<Desenvolvedora> buscarPorNome(String nome);
    List<Desenvolvedora> listar();
}
