package com.g4.game_tracker_backend.infraestrutura.persistencia.pedidos;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.StatusPedido;

@Repository
public class PedidoMarketplaceRepositorioPostgres implements PedidoMarketplaceRepositorio {

    private final PedidoMarketplaceSpringDataRepository springData;

    public PedidoMarketplaceRepositorioPostgres(PedidoMarketplaceSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public PedidoMarketplace salvar(PedidoMarketplace pedido) {
        springData.save(PedidoMarketplaceJpa.deDominio(pedido));
        return pedido;
    }

    @Override
    public Optional<PedidoMarketplace> buscarPorId(UUID id) {
        return springData.findById(id).map(PedidoMarketplaceJpa::paraDominio);
    }

    @Override
    public boolean existeAtivoParaAnuncio(UUID anuncioId) {
        return springData.existsByAnuncioIdAndStatusNot(anuncioId, StatusPedido.CANCELADO);
    }

    @Override
    public List<PedidoMarketplace> listarPorParticipante(UUID usuarioId) {
        return springData.listarPorParticipante(usuarioId).stream().map(PedidoMarketplaceJpa::paraDominio).toList();
    }

    @Override
    public List<PedidoMarketplace> listarComPrazoDeEnvioVencido(Instant agora) {
        return springData.findByStatusAndPrazoEnvioBefore(StatusPedido.AGUARDANDO_ENVIO, agora).stream()
                .map(PedidoMarketplaceJpa::paraDominio)
                .toList();
    }

    @Override
    public List<PedidoMarketplace> listarEnviadosAte(Instant limite) {
        return springData.findByStatusAndEnviadoEmLessThanEqual(StatusPedido.ENVIADO, limite).stream()
                .map(PedidoMarketplaceJpa::paraDominio)
                .toList();
    }
}
