package com.g4.game_tracker_backend.dominio.moderacao;

import java.util.Optional;
import java.util.UUID;

public interface EstadoConteudoRepositorio {
    Optional<EstadoConteudo> buscar(TipoConteudo tipo, UUID conteudoId);
    EstadoConteudo salvar(EstadoConteudo estado);
}
