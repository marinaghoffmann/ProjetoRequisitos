package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

/** Append-only interface: no update or delete operation is exposed for audit records. */
public interface LogModeracaoRepository extends Repository<LogModeracaoJpa, UUID> {
    LogModeracaoJpa save(LogModeracaoJpa novo);
    Optional<LogModeracaoJpa> findById(UUID id);
    List<LogModeracaoJpa> findAll();
    Page<LogModeracaoJpa> findAllByOrderByRegistradoEmDesc(Pageable page);
}
