package com.g4.game_tracker_backend.infraestrutura.persistencia.journal;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.g4.game_tracker_backend.dominio.journal.TipoEventoPlataforma;

public interface EntradaDeJournalSpringDataRepository extends JpaRepository<EntradaDeJournalJpa, UUID> {

    Optional<EntradaDeJournalJpa> findByTipoEventoAndOrigemId(TipoEventoPlataforma tipoEvento, String origemId);

    Page<EntradaDeJournalJpa> findByDonoId(UUID donoId, Pageable paginacao);
}
