package com.g4.game_tracker_backend.dominio.descoberta.similaridade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta ACL para o contexto de catálogo (F-01): traduz um jogo do catálogo para o PerfilJogo da engine, sem
 * a F-10 depender do modelo do catálogo.
 */
public interface PerfilJogoGateway {
    /** Perfil de um jogo ATIVO; vazio se o jogo não existe ou não está ativo. */
    Optional<PerfilJogo> obterPerfil(UUID jogoId);
    /** Perfis dos demais jogos ATIVOS do catálogo, candidatos a similares do jogo informado. */
    List<PerfilJogo> listarCandidatos(UUID jogoId);
}
