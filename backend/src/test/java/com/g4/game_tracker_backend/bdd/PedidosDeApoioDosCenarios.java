package com.g4.game_tracker_backend.bdd;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.marketplace.reputacao.FonteDePedidos;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.PedidoAvaliavel;

/**
 * Faz o papel da F-20 nos cenários da F-21 enquanto ela não existe. Quando a F-20 implementar
 * FonteDePedidos, apague esta classe e faça os steps de PassosReputacao criarem pedidos reais: as duas
 * implementações juntas falham na subida dos testes.
 */
@Component
public class PedidosDeApoioDosCenarios implements FonteDePedidos {

    private final Map<UUID, PedidoAvaliavel> pedidos = new ConcurrentHashMap<>();

    UUID criar(UUID compradorId, UUID vendedorId, boolean concluido, Instant entregaConfirmadaEm) {
        UUID id = UUID.randomUUID();
        pedidos.put(id, new PedidoAvaliavel(id, compradorId, vendedorId, concluido, entregaConfirmadaEm));
        return id;
    }

    @Override
    public Optional<PedidoAvaliavel> buscarPedido(UUID pedidoId) {
        return Optional.ofNullable(pedidos.get(pedidoId));
    }
}
