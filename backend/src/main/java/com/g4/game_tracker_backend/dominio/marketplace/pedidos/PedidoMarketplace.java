package com.g4.game_tracker_backend.dominio.marketplace.pedidos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Agregado do pedido do marketplace (F-20). É imutável: cada passo devolve uma nova instância.
 *
 * O pedido nasce já pago, com o valor EM_CUSTODIA (RN 7) e o prazo de envio de 3 dias úteis (RN 8). A
 * origem (RN 1), a unicidade por anúncio (RN 2) e a cobrança (RN 5) são checadas no checkout, antes de
 * criar o pedido, porque dependem de outros agregados.
 */
public final class PedidoMarketplace {

    public static final int DIAS_UTEIS_PARA_ENVIO = 3;
    public static final Duration TOLERANCIA_APOS_ENVIO = Duration.ofDays(7);

    private final UUID id;
    private final OrigemPedido origem;
    private final UUID anuncioId;
    private final UUID jogoId;
    private final String tituloAnuncio;
    private final UUID propostaId;
    private final String jogoOferecidoEmTroca;
    private final UUID compradorId;
    private final UUID vendedorId;
    private final BigDecimal valorItem;
    private final OpcaoFrete frete;
    private final BigDecimal total;
    private final EnderecoEntrega endereco;
    private final TransacaoPagamento pagamento;
    private final StatusPedido status;
    private final String codigoRastreio;
    private final Instant criadoEm;
    private final Instant prazoEnvio;
    private final Instant enviadoEm;
    private final Instant concluidoEm;
    private final Instant canceladoEm;

    private PedidoMarketplace(UUID id, OrigemPedido origem, UUID anuncioId, UUID jogoId, String tituloAnuncio,
            UUID propostaId, String jogoOferecidoEmTroca, UUID compradorId, UUID vendedorId, BigDecimal valorItem,
            OpcaoFrete frete, BigDecimal total, EnderecoEntrega endereco, TransacaoPagamento pagamento,
            StatusPedido status, String codigoRastreio, Instant criadoEm, Instant prazoEnvio, Instant enviadoEm,
            Instant concluidoEm, Instant canceladoEm) {
        this.id = Objects.requireNonNull(id, "O identificador do pedido é obrigatório");
        this.origem = Objects.requireNonNull(origem, "A origem do pedido é obrigatória");
        this.anuncioId = Objects.requireNonNull(anuncioId, "O anúncio é obrigatório");
        this.jogoId = Objects.requireNonNull(jogoId, "O jogo é obrigatório");
        this.tituloAnuncio = tituloAnuncio;
        this.propostaId = propostaId;
        this.jogoOferecidoEmTroca = jogoOferecidoEmTroca;
        this.compradorId = Objects.requireNonNull(compradorId, "O comprador é obrigatório");
        this.vendedorId = Objects.requireNonNull(vendedorId, "O vendedor é obrigatório");
        this.valorItem = Objects.requireNonNull(valorItem, "O valor do item é obrigatório");
        this.frete = Objects.requireNonNull(frete, "Escolha uma opção de frete");
        this.total = Objects.requireNonNull(total, "O total é obrigatório");
        this.endereco = Objects.requireNonNull(endereco, "O endereço de entrega é obrigatório");
        this.pagamento = Objects.requireNonNull(pagamento, "O pagamento é obrigatório");
        this.status = Objects.requireNonNull(status, "O status do pedido é obrigatório");
        this.codigoRastreio = codigoRastreio;
        this.criadoEm = Objects.requireNonNull(criadoEm, "A data do pedido é obrigatória");
        this.prazoEnvio = Objects.requireNonNull(prazoEnvio, "O prazo de envio é obrigatório");
        this.enviadoEm = enviadoEm;
        this.concluidoEm = concluidoEm;
        this.canceladoEm = canceladoEm;
    }

    /** RN 4: valor do item (preço ou valor da proposta) mais o frete, com duas casas decimais. */
    public static BigDecimal calcularTotal(BigDecimal valorItem, OpcaoFrete frete) {
        if (frete == null) {
            throw new RegraDeNegocioException("Escolha uma opção de frete");
        }
        return valorItem.add(frete.valor()).setScale(2, RoundingMode.HALF_UP);
    }

    /** O pedido nasce pago: o pagamento chega EM_CUSTODIA e o prazo de envio começa a contar (RN 8). */
    public static PedidoMarketplace gerar(OrigemPedido origem, UUID anuncioId, UUID jogoId, String tituloAnuncio,
            UUID propostaId, String jogoOferecidoEmTroca, UUID compradorId, UUID vendedorId, BigDecimal valorItem,
            OpcaoFrete frete, EnderecoEntrega endereco, TransacaoPagamento pagamento, Instant agora) {
        if (compradorId.equals(vendedorId)) {
            throw new RegraDeNegocioException("Você não pode comprar o próprio anúncio");
        }
        BigDecimal total = calcularTotal(valorItem, frete);
        if (pagamento.status() != StatusPagamento.EM_CUSTODIA || pagamento.valor().compareTo(total) != 0) {
            throw new IllegalArgumentException("O pagamento precisa estar em custódia e cobrir o total do pedido");
        }
        return new PedidoMarketplace(UUID.randomUUID(), origem, anuncioId, jogoId, tituloAnuncio, propostaId,
                jogoOferecidoEmTroca, compradorId, vendedorId, valorItem.setScale(2, RoundingMode.HALF_UP), frete,
                total, endereco, pagamento, StatusPedido.AGUARDANDO_ENVIO, null, agora,
                CalendarioDiasUteis.somarDiasUteis(agora, DIAS_UTEIS_PARA_ENVIO), null, null, null);
    }

    public static PedidoMarketplace reconstituir(UUID id, OrigemPedido origem, UUID anuncioId, UUID jogoId,
            String tituloAnuncio, UUID propostaId, String jogoOferecidoEmTroca, UUID compradorId, UUID vendedorId,
            BigDecimal valorItem, OpcaoFrete frete, BigDecimal total, EnderecoEntrega endereco,
            TransacaoPagamento pagamento, StatusPedido status, String codigoRastreio, Instant criadoEm,
            Instant prazoEnvio, Instant enviadoEm, Instant concluidoEm, Instant canceladoEm) {
        return new PedidoMarketplace(id, origem, anuncioId, jogoId, tituloAnuncio, propostaId, jogoOferecidoEmTroca,
                compradorId, vendedorId, valorItem, frete, total, endereco, pagamento, status, codigoRastreio,
                criadoEm, prazoEnvio, enviadoEm, concluidoEm, canceladoEm);
    }

    // ---- Partes ----

    public boolean parte(UUID usuarioId) {
        return compradorId.equals(usuarioId) || vendedorId.equals(usuarioId);
    }

    public void exigirParte(UUID usuarioId) {
        if (!parte(usuarioId)) {
            throw new AcessoNegadoException("Pedido restrito ao comprador e ao vendedor");
        }
    }

    // ---- Envio (RN 8) ----

    /** Só o vendedor registra o código de rastreio ou o método de entrega, dentro do prazo. */
    public PedidoMarketplace registrarEnvio(UUID usuarioId, String codigo, Instant agora) {
        exigirParte(usuarioId);
        if (!vendedorId.equals(usuarioId)) {
            throw new RegraDeNegocioException("Apenas o vendedor registra o envio");
        }
        if (status != StatusPedido.AGUARDANDO_ENVIO) {
            throw new RegraDeNegocioException("O envio deste pedido já foi registrado ou ele foi encerrado");
        }
        if (agora.isAfter(prazoEnvio)) {
            throw new RegraDeNegocioException("O prazo de 3 dias úteis para o envio terminou");
        }
        String rastreio = codigo == null ? "" : codigo.strip();
        if (rastreio.length() < 3 || rastreio.length() > 60) {
            throw new RegraDeNegocioException("O código de rastreio deve ter de 3 a 60 caracteres");
        }
        return com(StatusPedido.ENVIADO, pagamento, rastreio, agora, null, null);
    }

    // ---- Conclusão (RN 7, 10 e 11) ----

    /** RN 10: só o comprador confirma, e só com o pedido enviado; o valor sai da custódia para o vendedor. */
    public PedidoMarketplace confirmarRecebimento(UUID usuarioId, Instant agora) {
        exigirParte(usuarioId);
        if (!compradorId.equals(usuarioId)) {
            throw new RegraDeNegocioException("Apenas o comprador confirma o recebimento");
        }
        if (status != StatusPedido.ENVIADO) {
            throw new RegraDeNegocioException("Só é possível confirmar o recebimento de um pedido enviado");
        }
        return concluir(agora);
    }

    /** RN 7: sem confirmação, a custódia é liberada 7 dias após o registro do envio. */
    public boolean toleranciaVencida(Instant agora) {
        return status == StatusPedido.ENVIADO && !agora.isBefore(enviadoEm.plus(TOLERANCIA_APOS_ENVIO));
    }

    public PedidoMarketplace concluirPorTolerancia(Instant agora) {
        if (!toleranciaVencida(agora)) {
            throw new RegraDeNegocioException("A tolerância de 7 dias após o envio ainda não terminou");
        }
        return concluir(agora);
    }

    private PedidoMarketplace concluir(Instant agora) {
        return com(StatusPedido.CONCLUIDO, pagamento.liberar(agora), codigoRastreio, enviadoEm, agora, null);
    }

    // ---- Cancelamento (RN 9) ----

    public boolean prazoDeEnvioVencido(Instant agora) {
        return status == StatusPedido.AGUARDANDO_ENVIO && agora.isAfter(prazoEnvio);
    }

    /** Prazo de envio vencido sem rastreio: o pedido é cancelado e o pagamento reembolsado. */
    public PedidoMarketplace cancelarPorFaltaDeEnvio(Instant agora) {
        if (!prazoDeEnvioVencido(agora)) {
            throw new RegraDeNegocioException("O prazo de envio deste pedido ainda não terminou");
        }
        return com(StatusPedido.CANCELADO, pagamento.reembolsar(agora), null, null, null, agora);
    }

    private PedidoMarketplace com(StatusPedido novoStatus, TransacaoPagamento novoPagamento, String rastreio,
            Instant enviado, Instant concluido, Instant cancelado) {
        return new PedidoMarketplace(id, origem, anuncioId, jogoId, tituloAnuncio, propostaId, jogoOferecidoEmTroca,
                compradorId, vendedorId, valorItem, frete, total, endereco, novoPagamento, novoStatus, rastreio,
                criadoEm, prazoEnvio, enviado, concluido, cancelado);
    }

    public UUID id() { return id; }
    public OrigemPedido origem() { return origem; }
    public UUID anuncioId() { return anuncioId; }
    public UUID jogoId() { return jogoId; }
    public String tituloAnuncio() { return tituloAnuncio; }
    public UUID propostaId() { return propostaId; }
    public String jogoOferecidoEmTroca() { return jogoOferecidoEmTroca; }
    public UUID compradorId() { return compradorId; }
    public UUID vendedorId() { return vendedorId; }
    public BigDecimal valorItem() { return valorItem; }
    public OpcaoFrete frete() { return frete; }
    public BigDecimal total() { return total; }
    public EnderecoEntrega endereco() { return endereco; }
    public TransacaoPagamento pagamento() { return pagamento; }
    public StatusPedido status() { return status; }
    public String codigoRastreio() { return codigoRastreio; }
    public Instant criadoEm() { return criadoEm; }
    public Instant prazoEnvio() { return prazoEnvio; }
    public Instant enviadoEm() { return enviadoEm; }
    public Instant concluidoEm() { return concluidoEm; }
    public Instant canceladoEm() { return canceladoEm; }
}
