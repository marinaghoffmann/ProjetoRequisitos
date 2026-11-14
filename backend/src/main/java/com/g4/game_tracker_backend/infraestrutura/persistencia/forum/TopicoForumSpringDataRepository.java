package com.g4.game_tracker_backend.infraestrutura.persistencia.forum;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TopicoForumSpringDataRepository extends JpaRepository<TopicoForumJpa, UUID> {

    @Query("select t from TopicoForumJpa t left join fetch t.respostas where t.id = :id")
    Optional<TopicoForumJpa> buscarComRespostas(@Param("id") UUID id);

    @Query(value = """
            select t from TopicoForumJpa t
            where t.removido = false
              and (:jogoId is null or t.jogoId = :jogoId)
              and (:categoria = '' or lower(t.categoria) = lower(:categoria))
            """, countQuery = """
            select count(t) from TopicoForumJpa t
            where t.removido = false
              and (:jogoId is null or t.jogoId = :jogoId)
              and (:categoria = '' or lower(t.categoria) = lower(:categoria))
            """)
    Page<TopicoForumJpa> listar(@Param("jogoId") UUID jogoId, @Param("categoria") String categoria,
            Pageable paginacao);
}
