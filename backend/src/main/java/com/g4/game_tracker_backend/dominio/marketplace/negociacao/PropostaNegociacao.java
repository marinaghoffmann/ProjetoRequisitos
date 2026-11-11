package com.g4.game_tracker_backend.dominio.marketplace.negociacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Proposta formal dentro da conversa (F-19). Imutável: cada resposta devolve uma nova instância.
 *
 * RN 6: proposta de VALOR exige valor maior que zero; de TROCA, o nome do jogo oferecido (2 a 80).
 * RN 8: uma proposta respondida não muda mais, exceto a ACEITA, que pode expirar (RN 10).
 * RN 10: a ACEITA reserva o item por 48 horas para o comprador gerar o pedido (F-20).
 */
public final class PropostaNegociacao {

    public static final Duration PRAZO_PARA_PAGAR = Duration.ofHours(48);
    public static final String RESERVADO_PARA_OUTRO = "item reservado para outro comprador";
    public static final String PRAZO_ENCERRADO = "prazo de 48 horas para pagar encerrado";
    public static final String RESERVA_LIBERADA = "reserva liberada pelo vendedor";

    private static final String NAO_PENDENTE = "A proposta não está mais pendente";

    private final UUID id;
    private final TipoProposta tipo;
    private final BigDecimal valorOferecido;
    private final String jogoOferecido;
    private final StatusProposta status;
    private final String motivo;
    private final UUID pedidoId;
    private final Instant enviadaEm;
    private final Instant respondidaEm;

    private PropostaNegociacao(UUID id, TipoProposta tipo, BigDecimal valorOferecido, String jogoOferecido,
            StatusProposta status, String motivo, UUID pedidoId, Instant enviadaEm, Instant respondidaEm) {
        this.id = Objects.requireNonNull(id, "O identificador da proposta é obrigatório");
        this.tipo = Objects.requireNonNull(tipo, "O tipo da proposta é obrigatório");
        this.valorOferecido = valorOferecido;
        this.jogoOferecido = jogoOferecido;
        this.status = Objects.requireNonNull(status, "O status da proposta é obrigatório");
        this.motivo = motivo;
        this.pedidoId = pedidoId;
        this.enviadaEm = Objects.requireNonNull(enviadaEm, "A data de envio é obrigatória");
        this.respondidaEm = respondidaEm;
    }

    /** RN 6: toda proposta nasce PENDENTE. */
    static PropostaNegociacao nova(TipoProposta tipo, BigDecimal valor, String jogoOferecido, Instant agora) {
        if (tipo == null) {
            throw new RegraDeNegocioException("O tipo da proposta é obrigatório");
        }
        return switch (tipo) {
            case VALOR -> new PropostaNegociacao(UUID.randomUUID(), tipo, validarValor(valor), null,
                    StatusProposta.PENDENTE, null, null, agora, null);
            case TROCA -> new PropostaNegociacao(UUID.randomUUID(), tipo, null, validarJogo(jogoOferecido),
                    StatusProposta.PENDENTE, null, null, agora, null);
        };
    }

    public static PropostaNegociacao reconstituir(UUID id, TipoProposta tipo, BigDecimal valorOferecido,
            String jogoOferecido, StatusProposta status, String motivo, UUID pedidoId, Instant enviadaEm,
            Instant respondidaEm) {
        return new PropostaNegociacao(id, tipo, valorOferecido, jogoOferecido, status, motivo, pedidoId, enviadaEm,
                respondidaEm);
    }

    PropostaNegociacao aceitar(Instant agora) {
        return responder(StatusProposta.ACEITA, null, agora);
    }

    PropostaNegociacao recusar(String motivo, Instant agora) {
        return responder(StatusProposta.RECUSADA, motivo, agora);
    }

    PropostaNegociacao cancelar(Instant agora) {
        return responder(StatusProposta.CANCELADA, null, agora);
    }

    /** RN 10: a reserva acaba (prazo vencido ou liberada pelo vendedor) e a proposta expira. */
    PropostaNegociacao expirar(String motivo) {
        if (status != StatusProposta.ACEITA) {
            throw new RegraDeNegocioException("Só é possível liberar a reserva de uma proposta aceita");
        }
        if (pedidoId != null) {
            throw new RegraDeNegocioException("O pedido desta proposta já foi gerado");
        }
        return new PropostaNegociacao(id, tipo, valorOferecido, jogoOferecido, StatusProposta.EXPIRADA, motivo, null,
                enviadaEm, respondidaEm);
    }

    /** F-20: o comprador gerou o pedido dentro do prazo; a reserva não expira mais. */
    PropostaNegociacao vincularPedido(UUID pedidoId) {
        return new PropostaNegociacao(id, tipo, valorOferecido, jogoOferecido, status, motivo,
                Objects.requireNonNull(pedidoId, "O pedido é obrigatório"), enviadaEm, respondidaEm);
    }

    private PropostaNegociacao responder(StatusProposta novo, String motivo, Instant agora) {
        if (status != StatusProposta.PENDENTE) {
            throw new RegraDeNegocioException(NAO_PENDENTE);
        }
        return new PropostaNegociacao(id, tipo, valorOferecido, jogoOferecido, novo, motivo, null, enviadaEm, agora);
    }

    public boolean pendente() {
        return status == StatusProposta.PENDENTE;
    }

    /** Aceita, ainda sem pedido: o item está reservado para o comprador. */
    public boolean reservaVigente() {
        return status == StatusProposta.ACEITA && pedidoId == null;
    }

    /** RN 10: até quando o comprador pode gerar o pedido; nulo se a proposta não foi aceita. */
    public Instant prazoParaPagar() {
        return status == StatusProposta.ACEITA && respondidaEm != null ? respondidaEm.plus(PRAZO_PARA_PAGAR) : null;
    }

    public boolean reservaVencida(Instant agora) {
        return reservaVigente() && agora.isAfter(prazoParaPagar());
    }

    private static BigDecimal validarValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraDeNegocioException("O valor da proposta deve ser maior que zero");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new RegraDeNegocioException("O valor da proposta deve ter no máximo 2 casas decimais");
        }
        return valor.setScale(2, RoundingMode.UNNECESSARY);
    }

    private static String validarJogo(String jogo) {
        String normalizado = jogo == null ? "" : jogo.strip();
        if (normalizado.length() < 2 || normalizado.length() > 80) {
            throw new RegraDeNegocioException("O jogo oferecido na troca deve ter de 2 a 80 caracteres");
        }
        return normalizado;
    }

    public UUID id() { return id; }
    public TipoProposta tipo() { return tipo; }
    public BigDecimal valorOferecido() { return valorOferecido; }
    public String jogoOferecido() { return jogoOferecido; }
    public StatusProposta status() { return status; }
    public String motivo() { return motivo; }
    public UUID pedidoId() { return pedidoId; }
    public Instant enviadaEm() { return enviadaEm; }
    public Instant respondidaEm() { return respondidaEm; }
}
