package com.g4.game_tracker_backend.dominio.descoberta.similaridade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SimilaridadeRepositorio {
    SimilaridadeJogo salvar(SimilaridadeJogo similaridade);
    Optional<SimilaridadeJogo> buscarPorId(UUID id);
    /** RN 1: o par vale nos dois sentidos; a ordem dos jogos não importa. */
    Optional<SimilaridadeJogo> buscarPar(UUID jogoUm, UUID jogoDois);
    /** Todas as conexões do jogo, de qualquer status, consultando os dois lados do par. */
    List<SimilaridadeJogo> listarDoJogo(UUID jogoId);
    /** Se a similaridade do jogo já foi calculada alguma vez (há alguma conexão, de qualquer status). */
    boolean existeParaJogo(UUID jogoId);
}
