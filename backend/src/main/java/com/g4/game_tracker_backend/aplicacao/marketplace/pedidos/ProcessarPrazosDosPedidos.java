package com.g4.game_tracker_backend.aplicacao.marketplace.pedidos;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplaceRepositorio;

/**
 * Rotinas agendadas da F-20. Recebem o instante como parâmetro para os testes não dependerem do relógio, e
 * tratam cada pedido na sua própria transação: um pedido com problema não trava os demais.
 */
@Service
public class ProcessarPrazosDosPedidos {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessarPrazosDosPedidos.class);

    private final PedidoMarketplaceRepositorio pedidos;
    private final FormasDePagamento formas;
    private final CicloDoAnuncioNoPedido anuncios;
    private final ConcluirPedido concluirPedido;
    private final TransactionTemplate transacao;

    public ProcessarPrazosDosPedidos(PedidoMarketplaceRepositorio pedidos, FormasDePagamento formas,
            CicloDoAnuncioNoPedido anuncios, ConcluirPedido concluirPedido, TransactionTemplate transacao) {
        this.pedidos = pedidos;
        this.formas = formas;
        this.anuncios = anuncios;
        this.concluirPedido = concluirPedido;
        this.transacao = transacao;
    }

    /** RN 9: sem rastreio no prazo, o pedido é cancelado, o comprador reembolsado e o anúncio volta a ATIVO. */
    public int cancelarPedidosSemEnvio(Instant agora) {
        return processar(pedidos.listarComPrazoDeEnvioVencido(agora), pedido -> {
            PedidoMarketplace cancelado = pedidos.salvar(pedido.cancelarPorFaltaDeEnvio(agora));
            formas.para(cancelado.pagamento().forma()).reembolsar(cancelado.compradorId(), cancelado.pagamento(),
                    agora);
            anuncios.liberar(cancelado.anuncioId(), agora);
        });
    }

    /** RN 7: 7 dias após o envio sem confirmação, o pedido é concluído e o valor liberado ao vendedor. */
    public int liberarCustodiaPorTolerancia(Instant agora) {
        return processar(pedidos.listarEnviadosAte(agora.minus(PedidoMarketplace.TOLERANCIA_APOS_ENVIO)),
                pedido -> concluirPedido.registrar(pedido.concluirPorTolerancia(agora), agora));
    }

    private int processar(List<PedidoMarketplace> vencidos, Consumer<PedidoMarketplace> acao) {
        int processados = 0;
        for (PedidoMarketplace pedido : vencidos) {
            try {
                transacao.executeWithoutResult(status -> acao.accept(pedido));
                processados++;
            } catch (RuntimeException e) {
                LOG.error("Falha ao processar o prazo do pedido {}", pedido.id(), e);
            }
        }
        return processados;
    }
}
