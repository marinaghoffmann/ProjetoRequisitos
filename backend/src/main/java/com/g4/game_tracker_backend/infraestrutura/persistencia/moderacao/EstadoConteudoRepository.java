package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

public interface EstadoConteudoRepository extends JpaRepository<EstadoConteudoJpa, UUID> {

    Optional<EstadoConteudoJpa> findByTipoAndConteudoId(TipoConteudo tipo, UUID conteudoId);

    /** Quantos dos ids estão ocultos ou removidos (usado pela contagem de respostas do fórum). */
    long countByTipoAndConteudoIdInAndOcultoTrueOrTipoAndConteudoIdInAndRemovidoTrue(TipoConteudo tipo,
            Collection<UUID> ids, TipoConteudo tipo2, Collection<UUID> ids2);
}
