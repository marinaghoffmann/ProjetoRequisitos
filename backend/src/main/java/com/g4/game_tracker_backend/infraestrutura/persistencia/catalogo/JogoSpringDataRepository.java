package com.g4.game_tracker_backend.infraestrutura.persistencia.catalogo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.g4.game_tracker_backend.dominio.catalogo.OrigemJogo;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;

public interface JogoSpringDataRepository extends JpaRepository<JogoJpa, UUID> {

    Optional<JogoJpa> findByIdentificadorExterno(String identificadorExterno);

    boolean existsByTituloIgnoreCaseAndDesenvolvedora_Id(String titulo, UUID desenvolvedoraId);
    boolean existsByTituloIgnoreCaseAndDesenvolvedora_IdAndIdNot(String titulo, UUID desenvolvedoraId, UUID id);

    @Query("""
            select distinct j from JogoJpa j
            join j.desenvolvedora d
            left join j.generos g
            where lower(j.titulo) like lower(concat('%', :titulo, '%'))
              and (:generoId is null or g.id = :generoId)
              and (:desenvolvedoraId is null or d.id = :desenvolvedoraId)
              and (:origem is null or j.origem = :origem)
              and (:status is null or j.status = :status)
            order by j.titulo
            """)
    List<JogoJpa> consultar(@Param("titulo") String titulo, @Param("generoId") UUID generoId,
            @Param("desenvolvedoraId") UUID desenvolvedoraId, @Param("origem") OrigemJogo origem,
            @Param("status") StatusCatalogo status);
}
