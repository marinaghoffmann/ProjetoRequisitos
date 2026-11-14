package com.g4.game_tracker_backend.infraestrutura.persistencia.pedidos;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface CarteiraSpringDataRepository extends JpaRepository<CarteiraJpa, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CarteiraJpa c where c.donoId = :donoId")
    Optional<CarteiraJpa> buscarParaAtualizar(@Param("donoId") UUID donoId);
}
