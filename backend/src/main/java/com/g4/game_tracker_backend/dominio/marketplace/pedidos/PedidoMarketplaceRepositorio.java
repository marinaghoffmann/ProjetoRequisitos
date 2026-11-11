package com.g4.game_tracker_backend.dominio.marketplace.pedidos;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoMarketplaceRepositorio {

    PedidoMarketplace salvar(PedidoMarketplace pedido);

    Optional<PedidoMarketplace> buscarPorId(UUID id);

    /** RN 2: existe pedido não cancelado para o anúncio? */
    boolean existeAtivoParaAnuncio(UUID anuncioId);

    /** Compras e vendas do usuário, do mais recente para o mais antigo. */
    List<PedidoMarketplace> listarPorParticipante(UUID usuarioId);

    /** RN 9: aguardando envio com o prazo de 3 dias úteis vencido. */
    List<PedidoMarketplace> listarComPrazoDeEnvioVencido(Instant agora);

    /** RN 7: enviados até o limite (agora menos 7 dias). */
    List<PedidoMarketplace> listarEnviadosAte(Instant limite);
}
