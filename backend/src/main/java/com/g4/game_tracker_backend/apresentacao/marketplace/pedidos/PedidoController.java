package com.g4.game_tracker_backend.apresentacao.marketplace.pedidos;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.AcompanharPedido;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.ConsultarPedidos;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.GerarPedido;
import com.g4.game_tracker_backend.aplicacao.marketplace.pedidos.PrepararCheckout;

/** F-20: todas as rotas exigem login; um pedido só responde ao comprador e ao vendedor dele. */
@RestController
@RequestMapping("/api/marketplace")
public class PedidoController {

    private final PrepararCheckout prepararCheckout;
    private final GerarPedido gerarPedido;
    private final AcompanharPedido acompanharPedido;
    private final ConsultarPedidos consultarPedidos;

    public PedidoController(PrepararCheckout prepararCheckout, GerarPedido gerarPedido,
            AcompanharPedido acompanharPedido, ConsultarPedidos consultarPedidos) {
        this.prepararCheckout = prepararCheckout;
        this.gerarPedido = gerarPedido;
        this.acompanharPedido = acompanharPedido;
        this.consultarPedidos = consultarPedidos;
    }

    /** Prévia do checkout: confere a origem do pedido (RN 1 e RN 2) sem cobrar. */
    @GetMapping("/checkout")
    public CheckoutResponse checkout(@RequestParam(required = false) UUID anuncioId,
            @RequestParam(required = false) UUID propostaId) {
        return CheckoutResponse.de(prepararCheckout.executar(anuncioId, propostaId));
    }

    @PostMapping("/pedidos")
    public ResponseEntity<PedidoResponse> gerar(@RequestBody PedidoRequest request) {
        UUID pedidoId = gerarPedido.executar(request.anuncioId(), request.propostaId(), request.enderecoDeEntrega(),
                request.frete(), request.formaPagamento(), request.cartaoSimulado()).id();
        return ResponseEntity.created(URI.create("/api/marketplace/pedidos/" + pedidoId))
                .body(PedidoResponse.de(consultarPedidos.detalhar(pedidoId)));
    }

    /** Compras e vendas do usuário, do mais recente para o mais antigo. */
    @GetMapping("/pedidos")
    public List<PedidoResponse> listar() {
        return consultarPedidos.listar().stream().map(PedidoResponse::de).toList();
    }

    @GetMapping("/pedidos/{id}")
    public PedidoResponse consultar(@PathVariable UUID id) {
        return PedidoResponse.de(consultarPedidos.detalhar(id));
    }

    @PostMapping("/pedidos/{id}/envio")
    public PedidoResponse registrarEnvio(@PathVariable UUID id, @RequestBody EnvioRequest request) {
        acompanharPedido.registrarEnvio(id, request.codigoRastreio());
        return PedidoResponse.de(consultarPedidos.detalhar(id));
    }

    @PostMapping("/pedidos/{id}/recebimento")
    public PedidoResponse confirmarRecebimento(@PathVariable UUID id) {
        acompanharPedido.confirmarRecebimento(id);
        return PedidoResponse.de(consultarPedidos.detalhar(id));
    }

    @GetMapping("/carteira")
    public Map<String, BigDecimal> carteira() {
        return Map.of("saldo", consultarPedidos.saldoDaCarteira());
    }
}
