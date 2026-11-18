package com.g4.game_tracker_backend.infraestrutura.persistencia.similaridade;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.g4.game_tracker_backend.dominio.descoberta.similaridade.TipoFeedback;

public interface FeedbackSugestaoSpringDataRepository extends JpaRepository<FeedbackSugestaoJpa, UUID> {

    Optional<FeedbackSugestaoJpa> findByUsuarioIdAndSimilaridadeId(UUID usuarioId, UUID similaridadeId);

    boolean existsByUsuarioIdAndSimilaridadeId(UUID usuarioId, UUID similaridadeId);

    long countBySimilaridadeIdAndTipo(UUID similaridadeId, TipoFeedback tipo);

    List<FeedbackSugestaoJpa> findByUsuarioIdAndSimilaridadeIdIn(UUID usuarioId, Collection<UUID> similaridadeIds);
}
