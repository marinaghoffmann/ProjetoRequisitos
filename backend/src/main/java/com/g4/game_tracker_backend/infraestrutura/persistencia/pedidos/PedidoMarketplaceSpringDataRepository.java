package com.g4.game_tracker_backend.infraestrutura.persistencia.pedidos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.g4.game_tracker_backend.dominio.marketplace.pedidos.StatusPedido;

public interface PedidoMarketplaceSpringDataRepository extends JpaRepository<PedidoMarketplaceJpa, UUID> {

    boolean existsByAnuncioIdAndStatusNot(UUID anuncioId, StatusPedido status);

    @Query("""
            select p from PedidoMarketplaceJpa p
            where p.compradorId = :usuarioId or p.vendedorId = :usuarioId
            order by p.criadoEm desc, p.id desc
            """)
    List<PedidoMarketplaceJpa> listarPorParticipante(@Param("usuarioId") UUID usuarioId);

    List<PedidoMarketplaceJpa> findByStatusAndPrazoEnvioBefore(StatusPedido status, Instant agora);

    List<PedidoMarketplaceJpa> findByStatusAndEnviadoEmLessThanEqual(StatusPedido status, Instant limite);
}
