package com.g4.game_tracker_backend.infraestrutura.persistencia.reviews;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewSpringDataRepository extends JpaRepository<ReviewJpa, UUID> {

    Optional<ReviewJpa> findByAutorIdAndJogoId(UUID autorId, UUID jogoId);

    @Query("select r from ReviewJpa r where r.jogoId = :jogoId and not exists (select 1 from EstadoConteudoJpa m where m.tipo = com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo.REVIEW and m.conteudoId = r.id and (m.oculto = true or m.removido = true))")
    Page<ReviewJpa> findByJogoId(@Param("jogoId") UUID jogoId, Pageable paginacao);

    List<ReviewJpa> findByAutorIdOrderByCriadaEmDesc(UUID autorId);

    /** Uma linha: soma e quantidade das notas gerais do jogo. */
    @Query("select coalesce(sum(r.notaGeral), 0), count(r) from ReviewJpa r where r.jogoId = :jogoId and not exists (select 1 from EstadoConteudoJpa m where m.tipo = com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo.REVIEW and m.conteudoId = r.id and (m.oculto = true or m.removido = true))")
    List<Object[]> somarNotasGerais(@Param("jogoId") UUID jogoId);

    /** Uma linha por categoria avaliada: categoria, soma e quantidade. */
    @Query("""
            select n.categoria, sum(n.nota), count(n)
            from ReviewJpa r join r.notas n
            where r.jogoId = :jogoId
              and not exists (select 1 from EstadoConteudoJpa m where m.tipo = com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo.REVIEW and m.conteudoId = r.id and (m.oculto = true or m.removido = true))
            group by n.categoria
            """)
    List<Object[]> somarNotasPorCategoria(@Param("jogoId") UUID jogoId);
    boolean existsByJogoId(UUID jogoId);
}
