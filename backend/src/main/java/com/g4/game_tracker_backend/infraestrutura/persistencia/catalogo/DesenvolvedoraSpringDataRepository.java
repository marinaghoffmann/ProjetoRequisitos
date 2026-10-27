package com.g4.game_tracker_backend.infraestrutura.persistencia.catalogo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DesenvolvedoraSpringDataRepository extends JpaRepository<DesenvolvedoraJpa, UUID> {
    Optional<DesenvolvedoraJpa> findByNomeIgnoreCase(String nome);
}
