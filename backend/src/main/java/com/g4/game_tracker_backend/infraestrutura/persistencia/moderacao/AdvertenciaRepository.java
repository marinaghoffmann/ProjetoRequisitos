package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AdvertenciaRepository extends JpaRepository<AdvertenciaJpa,UUID> { List<AdvertenciaJpa> findByUsuarioIdOrderByRegistradaEmDesc(UUID usuarioId); long countByUsuarioId(UUID usuarioId); }
