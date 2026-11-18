package com.g4.game_tracker_backend.infraestrutura.persistencia.journal;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import com.g4.game_tracker_backend.dominio.journal.TipoEventoPlataforma;

public interface EntradaDeJournalSpringDataRepository extends JpaRepository<EntradaDeJournalJpa, UUID>,
        JpaSpecificationExecutor<EntradaDeJournalJpa> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EntradaDeJournalJpa e where e.id = :id")
    Optional<EntradaDeJournalJpa> buscarParaAlterar(UUID id);

    @Query("select distinct t from EntradaDeJournalJpa e join e.tags t where e.donoId = :donoId order by t")
    List<String> listarTags(UUID donoId);

    Optional<EntradaDeJournalJpa> findByTipoEventoAndOrigemId(TipoEventoPlataforma tipoEvento, String origemId);

    Page<EntradaDeJournalJpa> findByDonoId(UUID donoId, Pageable paginacao);
}
