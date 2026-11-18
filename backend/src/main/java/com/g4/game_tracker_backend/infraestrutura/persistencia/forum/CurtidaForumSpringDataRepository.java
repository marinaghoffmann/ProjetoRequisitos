package com.g4.game_tracker_backend.infraestrutura.persistencia.forum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CurtidaForumSpringDataRepository extends JpaRepository<CurtidaForumJpa, UUID> {
    Optional<CurtidaForumJpa> findByUsuarioIdAndTipoAndConteudoId(UUID usuarioId, String tipo, UUID conteudoId);
    List<CurtidaForumJpa> findAllByTopicoId(UUID topicoId);
    long countByTopicoId(UUID topicoId);
    void deleteByTipoAndConteudoId(String tipo, UUID conteudoId);
}
