package com.g4.game_tracker_backend.infraestrutura.persistencia.reputacao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ReputacaoUsuarioSpringDataRepository extends JpaRepository<ReputacaoUsuarioJpa, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReputacaoUsuarioJpa r where r.usuarioId = :usuarioId")
    Optional<ReputacaoUsuarioJpa> buscarParaAtualizar(@Param("usuarioId") UUID usuarioId);
}
