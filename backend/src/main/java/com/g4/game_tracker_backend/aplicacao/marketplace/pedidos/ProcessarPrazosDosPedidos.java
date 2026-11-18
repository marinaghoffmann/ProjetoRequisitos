package com.g4.game_tracker_backend.aplicacao.marketplace.pedidos;

import java.time.Instant;
import java.util.List;

import com.g4.game_tracker_backend.aplicacao.compartilhado.ExecutorDeTransacao;
import com.g4.game_tracker_backend.aplicacao.compartilhado.RotinaDePrazo;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplaceRepositorio;

/**
 * Rotinas agendadas da F-20. Cada uma é uma {@link RotinaDePrazo} (Template Method): recebe o instante como
 * parâmetro, para os testes não dependerem do relógio, e trata cada pedido na sua própria transação, para
 * um pedido com problema não travar os demais.
 */
public class ProcessarPrazosDosPedidos {

    private final CancelarPedidosSemEnvio cancelarSemEnvio;
    private final LiberarCustodiaPorTolerancia liberarCustodia;

    public ProcessarPrazosDosPedidos(PedidoMarketplaceRepositorio pedidos, FormasDePagamento formas,
            CicloDoAnuncioNoPedido anuncios, ConcluirPedido concluirPedido, ExecutorDeTransacao transacao) {
        this.cancelarSemEnvio = new CancelarPedidosSemEnvio(pedidos, formas, anuncios, transacao);
        this.liberarCustodia = new LiberarCustodiaPorTolerancia(pedidos, concluirPedido, transacao);
    }

    /** RN 9: sem rastreio no prazo, o pedido é cancelado, o comprador reembolsado e o anúncio volta a ATIVO. */
    public int cancelarPedidosSemEnvio(Instant agora) {
        return cancelarSemEnvio.executar(agora);
    }

    /** RN 7: 7 dias após o envio sem confirmação, o pedido é concluído e o valor liberado ao vendedor. */
    public int liberarCustodiaPorTolerancia(Instant agora) {
        return liberarCustodia.executar(agora);
    }

    private static final class CancelarPedidosSemEnvio extends RotinaDePrazo<PedidoMarketplace> {

        private final PedidoMarketplaceRepositorio pedidos;
        private final FormasDePagamento formas;
        private final CicloDoAnuncioNoPedido anuncios;

        CancelarPedidosSemEnvio(PedidoMarketplaceRepositorio pedidos, FormasDePagamento formas,
                CicloDoAnuncioNoPedido anuncios, ExecutorDeTransacao transacao) {
            super(transacao);
            this.pedidos = pedidos;
            this.formas = formas;
            this.anuncios = anuncios;
        }

        @Override
        protected List<PedidoMarketplace> vencidos(Instant agora) {
            return pedidos.listarComPrazoDeEnvioVencido(agora);
        }

        @Override
        protected boolean tratar(PedidoMarketplace pedido, Instant agora) {
            PedidoMarketplace cancelado = pedidos.salvar(pedido.cancelarPorFaltaDeEnvio(agora));
            formas.para(cancelado.pagamento().forma()).reembolsar(cancelado.compradorId(), cancelado.pagamento(),
                    agora);
            anuncios.liberar(cancelado.anuncioId(), agora);
            return true;
        }

        @Override
        protected String descrever(PedidoMarketplace pedido) {
            return "envio do pedido " + pedido.id();
        }
    }

    private static final class LiberarCustodiaPorTolerancia extends RotinaDePrazo<PedidoMarketplace> {

        private final PedidoMarketplaceRepositorio pedidos;
        private final ConcluirPedido concluirPedido;

        LiberarCustodiaPorTolerancia(PedidoMarketplaceRepositorio pedidos, ConcluirPedido concluirPedido,
                ExecutorDeTransacao transacao) {
            super(transacao);
            this.pedidos = pedidos;
            this.concluirPedido = concluirPedido;
        }

        @Override
        protected List<PedidoMarketplace> vencidos(Instant agora) {
            return pedidos.listarEnviadosAte(agora.minus(PedidoMarketplace.TOLERANCIA_APOS_ENVIO));
        }

        @Override
        protected boolean tratar(PedidoMarketplace pedido, Instant agora) {
            concluirPedido.registrar(pedido.concluirPorTolerancia(agora), agora);
            return true;
        }

        @Override
        protected String descrever(PedidoMarketplace pedido) {
            return "custódia do pedido " + pedido.id();
        }
    }
}
