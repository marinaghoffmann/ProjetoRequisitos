package com.g4.game_tracker_backend.infraestrutura.persistencia.negociacao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversaChatSpringDataRepository extends JpaRepository<ConversaChatJpa, UUID> {

    Optional<ConversaChatJpa> findByAnuncioIdAndCompradorId(UUID anuncioId, UUID compradorId);

    List<ConversaChatJpa> findByAnuncioId(UUID anuncioId);

    @Query("select c from ConversaChatJpa c join c.propostas p where p.id = :propostaId")
    Optional<ConversaChatJpa> buscarPorProposta(@Param("propostaId") UUID propostaId);

    @Query("""
            select c from ConversaChatJpa c
            where c.compradorId = :usuarioId or c.vendedorId = :usuarioId
            order by c.ultimaAtividadeEm desc, c.id desc
            """)
    List<ConversaChatJpa> listarPorParticipante(@Param("usuarioId") UUID usuarioId);

    @Query("""
            select distinct c from ConversaChatJpa c join c.propostas p
            where p.status = com.g4.game_tracker_backend.dominio.marketplace.negociacao.StatusProposta.ACEITA
              and p.pedidoId is null
              and p.respondidaEm <= :limite
            """)
    List<ConversaChatJpa> listarComReservaAceitaAte(@Param("limite") Instant limite);
}
