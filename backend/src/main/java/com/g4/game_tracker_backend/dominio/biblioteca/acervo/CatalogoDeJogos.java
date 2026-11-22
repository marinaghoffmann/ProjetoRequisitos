package com.g4.game_tracker_backend.dominio.biblioteca.acervo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Porta de leitura do catálogo (outro contexto); a infraestrutura adapta o repositório do catálogo. */
public interface CatalogoDeJogos {

    record JogoResumo(UUID id, String titulo, String capaUrl) {}

    Optional<JogoResumo> buscarResumo(UUID jogoId);

    List<JogoResumo> buscarResumos(Collection<UUID> jogoIds);
}