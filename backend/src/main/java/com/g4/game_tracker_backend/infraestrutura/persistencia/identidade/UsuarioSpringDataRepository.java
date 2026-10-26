package com.g4.game_tracker_backend.infraestrutura.persistencia.identidade;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioSpringDataRepository extends JpaRepository<UsuarioJpa, UUID> {

    Optional<UsuarioJpa> findByEmail(String email);

    boolean existsByEmail(String email);
}
