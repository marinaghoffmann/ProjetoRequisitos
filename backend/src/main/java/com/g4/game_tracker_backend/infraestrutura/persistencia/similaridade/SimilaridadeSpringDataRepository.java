package com.g4.game_tracker_backend.infraestrutura.persistencia.similaridade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SimilaridadeSpringDataRepository extends JpaRepository<SimilaridadeJogoJpa, UUID> {

    Optional<SimilaridadeJogoJpa> findByJogoAIdAndJogoBId(UUID jogoAId, UUID jogoBId);

    @Query("select s from SimilaridadeJogoJpa s where s.jogoAId = :jogoId or s.jogoBId = :jogoId")
    List<SimilaridadeJogoJpa> listarDoJogo(@Param("jogoId") UUID jogoId);

    @Query("select count(s) > 0 from SimilaridadeJogoJpa s where s.jogoAId = :jogoId or s.jogoBId = :jogoId")
    boolean existeParaJogo(@Param("jogoId") UUID jogoId);
}
