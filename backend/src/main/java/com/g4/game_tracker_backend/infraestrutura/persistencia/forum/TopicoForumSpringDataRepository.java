package com.g4.game_tracker_backend.infraestrutura.persistencia.forum;

import java.util.Optional;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.forum.StatusTopico;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface TopicoForumSpringDataRepository extends JpaRepository<TopicoForumJpa, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TopicoForumJpa t where t.id = :id")
    Optional<TopicoForumJpa> bloquearTopico(@Param("id") UUID id);

    @Query("select t from TopicoForumJpa t left join fetch t.respostas where t.id = :id")
    Optional<TopicoForumJpa> buscarComRespostas(@Param("id") UUID id);

    @Modifying
    @Query("update TopicoForumJpa t set t.totalCurtidas = :total where t.id = :id")
    void atualizarCurtidas(@Param("id") UUID id, @Param("total") int total);

    @Query(value = """
            select t from TopicoForumJpa t
            where t.removido = false
              and (:jogoId is null or t.jogoId = :jogoId)
              and (:apenasGerais = false or t.jogoId is null)
              and (:categoria = '' or lower(t.categoria) = lower(:categoria)
                   or (:categoria = 'Discussão geral' and t.categoria = 'Geral'))
              and (:status is null or t.status = :status)
            order by case when :emAlta = true then t.totalRespostas + t.totalCurtidas else 0 end desc,
                t.dataUltimaAtividade desc, t.id desc
            """, countQuery = """
            select count(t) from TopicoForumJpa t
            where t.removido = false
              and (:jogoId is null or t.jogoId = :jogoId)
              and (:apenasGerais = false or t.jogoId is null)
              and (:categoria = '' or lower(t.categoria) = lower(:categoria)
                   or (:categoria = 'Discussão geral' and t.categoria = 'Geral'))
              and (:status is null or t.status = :status)
            """)
    Page<TopicoForumJpa> listar(@Param("jogoId") UUID jogoId, @Param("categoria") String categoria,
            @Param("apenasGerais") boolean apenasGerais,
            @Param("status") StatusTopico status,
            @Param("emAlta") boolean emAlta, Pageable paginacao);
}
