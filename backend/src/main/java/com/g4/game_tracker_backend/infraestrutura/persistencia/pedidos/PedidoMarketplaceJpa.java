package com.g4.game_tracker_backend.infraestrutura.persistencia.pedidos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.marketplace.pedidos.EnderecoEntrega;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.FormaPagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.OpcaoFrete;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.OrigemPedido;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.PedidoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.StatusPagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.StatusPedido;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.TransacaoPagamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Pedido com o endereço e o pagamento na mesma linha (os dois só existem dentro do pedido). Os índices por
 * status atendem às rotinas de prazo (RN 7 e RN 9); o de anúncio, à checagem de pedido ativo (RN 2).
 */
@Entity
@Table(name = "marketplace_pedidos", indexes = {
        @Index(name = "ix_marketplace_pedidos_comprador", columnList = "comprador_id"),
        @Index(name = "ix_marketplace_pedidos_vendedor", columnList = "vendedor_id"),
        @Index(name = "ix_marketplace_pedidos_anuncio", columnList = "anuncio_id, status"),
        @Index(name = "ix_marketplace_pedidos_prazo_envio", columnList = "status, prazo_envio"),
        @Index(name = "ix_marketplace_pedidos_enviado", columnList = "status, enviado_em")
})
public class PedidoMarketplaceJpa {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrigemPedido origem;

    @Column(name = "anuncio_id", nullable = false)
    private UUID anuncioId;

    @Column(name = "jogo_id", nullable = false)
    private UUID jogoId;

    @Column(name = "titulo_anuncio", length = 180)
    private String tituloAnuncio;

    @Column(name = "proposta_id")
    private UUID propostaId;

    @Column(name = "jogo_oferecido_em_troca", length = 80)
    private String jogoOferecidoEmTroca;

    @Column(name = "comprador_id", nullable = false)
    private UUID compradorId;

    @Column(name = "vendedor_id", nullable = false)
    private UUID vendedorId;

    @Column(name = "valor_item", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OpcaoFrete frete;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "endereco_destinatario", nullable = false, length = 80)
    private String destinatario;

    @Column(name = "endereco_cep", nullable = false, length = 9)
    private String cep;

    @Column(name = "endereco_rua", nullable = false, length = 120)
    private String rua;

    @Column(name = "endereco_numero", nullable = false, length = 10)
    private String numero;

    @Column(name = "endereco_complemento", length = 60)
    private String complemento;

    @Column(name = "endereco_cidade", nullable = false, length = 80)
    private String cidade;

    @Column(name = "endereco_uf", nullable = false, length = 2)
    private String uf;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 16)
    private FormaPagamento formaPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pagamento", nullable = false, length = 16)
    private StatusPagamento statusPagamento;

    @Column(name = "final_cartao", length = 4)
    private String finalCartao;

    @Column(name = "pago_em", nullable = false)
    private Instant pagoEm;

    @Column(name = "liberado_em")
    private Instant liberadoEm;

    @Column(name = "reembolsado_em")
    private Instant reembolsadoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status;

    @Column(name = "codigo_rastreio", length = 60)
    private String codigoRastreio;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "prazo_envio", nullable = false)
    private Instant prazoEnvio;

    @Column(name = "enviado_em")
    private Instant enviadoEm;

    @Column(name = "concluido_em")
    private Instant concluidoEm;

    @Column(name = "cancelado_em")
    private Instant canceladoEm;

    protected PedidoMarketplaceJpa() {
    }

    private PedidoMarketplaceJpa(PedidoMarketplace pedido) {
        this.id = pedido.id();
        this.origem = pedido.origem();
        this.anuncioId = pedido.anuncioId();
        this.jogoId = pedido.jogoId();
        this.tituloAnuncio = pedido.tituloAnuncio();
        this.propostaId = pedido.propostaId();
        this.jogoOferecidoEmTroca = pedido.jogoOferecidoEmTroca();
        this.compradorId = pedido.compradorId();
        this.vendedorId = pedido.vendedorId();
        this.valorItem = pedido.valorItem();
        this.frete = pedido.frete();
        this.total = pedido.total();
        EnderecoEntrega endereco = pedido.endereco();
        this.destinatario = endereco.destinatario();
        this.cep = endereco.cep();
        this.rua = endereco.rua();
        this.numero = endereco.numero();
        this.complemento = endereco.complemento();
        this.cidade = endereco.cidade();
        this.uf = endereco.uf();
        TransacaoPagamento pagamento = pedido.pagamento();
        this.formaPagamento = pagamento.forma();
        this.statusPagamento = pagamento.status();
        this.finalCartao = pagamento.finalCartao();
        this.pagoEm = pagamento.pagoEm();
        this.liberadoEm = pagamento.liberadoEm();
        this.reembolsadoEm = pagamento.reembolsadoEm();
        this.status = pedido.status();
        this.codigoRastreio = pedido.codigoRastreio();
        this.criadoEm = pedido.criadoEm();
        this.prazoEnvio = pedido.prazoEnvio();
        this.enviadoEm = pedido.enviadoEm();
        this.concluidoEm = pedido.concluidoEm();
        this.canceladoEm = pedido.canceladoEm();
    }

    static PedidoMarketplaceJpa deDominio(PedidoMarketplace pedido) {
        return new PedidoMarketplaceJpa(pedido);
    }

    PedidoMarketplace paraDominio() {
        return PedidoMarketplace.reconstituir(id, origem, anuncioId, jogoId, tituloAnuncio, propostaId,
                jogoOferecidoEmTroca, compradorId, vendedorId, valorItem, frete, total,
                new EnderecoEntrega(destinatario, cep, rua, numero, complemento, cidade, uf),
                new TransacaoPagamento(formaPagamento, total, statusPagamento, finalCartao, pagoEm, liberadoEm,
                        reembolsadoEm),
                status, codigoRastreio, criadoEm, prazoEnvio, enviadoEm, concluidoEm, canceladoEm);
    }
}
