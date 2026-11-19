package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

/** Só acrescenta: não expõe operação de alteração nem de exclusão dos registros de auditoria. */
interface LogModeracaoRepository extends Repository<LogModeracaoJpa, UUID> {

    LogModeracaoJpa save(LogModeracaoJpa novo);

    Optional<LogModeracaoJpa> findById(UUID id);

    List<LogModeracaoJpa> findByConteudoIdOrderByRegistradoEmAsc(UUID conteudoId);

    Page<LogModeracaoJpa> findAllByOrderByRegistradoEmDesc(Pageable pagina);
}
