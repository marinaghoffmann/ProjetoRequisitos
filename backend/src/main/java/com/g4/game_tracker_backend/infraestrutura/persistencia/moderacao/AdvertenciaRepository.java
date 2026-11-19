package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface AdvertenciaRepository extends JpaRepository<AdvertenciaJpa, UUID> {

    List<AdvertenciaJpa> findByUsuarioIdOrderByRegistradaEmDesc(UUID usuarioId);

    long countByUsuarioId(UUID usuarioId);
}
