package com.g4.game_tracker_backend.infraestrutura.persistencia.listas;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ListaPersonalizadaSpringDataRepository extends JpaRepository<ListaPersonalizadaJpa, UUID> {

    List<ListaPersonalizadaJpa> findByDonoIdOrderByAtualizadaEmDescIdDesc(UUID donoId);

    @Query("""
            select count(l) > 0 from ListaPersonalizadaJpa l
            where l.donoId = :donoId and l.tituloChave = :tituloChave
              and (:listaIgnorada is null or l.id <> :listaIgnorada)
            """)
    boolean existeTitulo(@Param("donoId") UUID donoId, @Param("tituloChave") String tituloChave,
            @Param("listaIgnorada") UUID listaIgnorada);
}
