package com.g4.game_tracker_backend.apresentacao.marketplace.pedidos;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.CartaoSimulado;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.EnderecoEntrega;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.FormaPagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.OpcaoFrete;

/**
 * Checkout: anuncioId (compra direta) ou propostaId (proposta aceita no chat). cartao só para CARTAO. As
 * regras ficam no domínio, para as mensagens serem as mesmas em qualquer canal.
 */
public record PedidoRequest(UUID anuncioId, UUID propostaId, EnderecoRequest endereco, OpcaoFrete frete,
        FormaPagamento formaPagamento, CartaoRequest cartao) {

    EnderecoEntrega enderecoDeEntrega() {
        if (endereco == null) {
            throw new RegraDeNegocioException("Informe o endereço de entrega");
        }
        return new EnderecoEntrega(endereco.destinatario(), endereco.cep(), endereco.rua(), endereco.numero(),
                endereco.complemento(), endereco.cidade(), endereco.uf());
    }

    CartaoSimulado cartaoSimulado() {
        return cartao == null ? null
                : new CartaoSimulado(cartao.numero(), cartao.nomeImpresso(), cartao.validade(), cartao.cvv());
    }

    public record EnderecoRequest(String destinatario, String cep, String rua, String numero, String complemento,
            String cidade, String uf) {
    }

    public record CartaoRequest(String numero, String nomeImpresso, String validade, String cvv) {
    }
}
