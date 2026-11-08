package com.g4.game_tracker_backend.infraestrutura.persistencia.biblioteca;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface JogandoAgoraSpringData extends JpaRepository<JogandoAgoraJpa, UUID> {

    List<JogandoAgoraJpa> findByUsuarioIdAndAtivoTrueOrderByDataInicioDesc(UUID usuarioId);

    Optional<JogandoAgoraJpa> findByUsuarioIdAndJogoIdAndAtivoTrue(UUID usuarioId, UUID jogoId);

    long countByUsuarioIdAndAtivoTrue(UUID usuarioId);
}