package com.g4.game_tracker_backend.infraestrutura.persistencia.catalogo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneroSpringDataRepository extends JpaRepository<GeneroJpa, UUID> {
    Optional<GeneroJpa> findByNomeIgnoreCase(String nome);
}
