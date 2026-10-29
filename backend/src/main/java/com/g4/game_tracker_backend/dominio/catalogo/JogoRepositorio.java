package com.g4.game_tracker_backend.dominio.catalogo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JogoRepositorio {
    Jogo salvar(Jogo jogo);
    Optional<Jogo> buscarPorId(UUID id);
    Optional<Jogo> buscarPorIdentificadorExterno(String identificadorExterno);
    boolean existePorTituloEDesenvolvedora(String titulo, UUID desenvolvedoraId);
    boolean existePorTituloEDesenvolvedoraEIdDiferente(String titulo, UUID desenvolvedoraId, UUID jogoId);
    List<Jogo> consultar(FiltroCatalogo filtro);
}
