package com.g4.game_tracker_backend.infraestrutura.persistencia.estatisticas;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EstatisticaJogoSpringDataRepository extends JpaRepository<EstatisticaJogoJpa, UUID> {
}
