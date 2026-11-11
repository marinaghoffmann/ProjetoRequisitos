package com.g4.game_tracker_backend.dominio.marketplace.pedidos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Carteira interna do usuário (F-20 RN 6). Imutável. É criada na primeira transação com um saldo inicial de
 * simulação; paga compras com "Saldo da Carteira" e recebe os valores liberados nas vendas.
 */
public final class Carteira {

    public static final BigDecimal SALDO_INICIAL = new BigDecimal("500.00");

    private final UUID donoId;
    private final BigDecimal saldo;
    private final Instant criadaEm;
    private final Instant atualizadaEm;

    private Carteira(UUID donoId, BigDecimal saldo, Instant criadaEm, Instant atualizadaEm) {
        this.donoId = Objects.requireNonNull(donoId, "O dono da carteira é obrigatório");
        this.saldo = Objects.requireNonNull(saldo, "O saldo é obrigatório").setScale(2, RoundingMode.HALF_UP);
        if (this.saldo.signum() < 0) {
            throw new RegraDeNegocioException("O saldo da carteira não pode ficar negativo");
        }
        this.criadaEm = Objects.requireNonNull(criadaEm, "A data de criação é obrigatória");
        this.atualizadaEm = Objects.requireNonNull(atualizadaEm, "A data de atualização é obrigatória");
    }

    public static Carteira abrir(UUID donoId, Instant agora) {
        return new Carteira(donoId, SALDO_INICIAL, agora, agora);
    }

    public static Carteira reconstituir(UUID donoId, BigDecimal saldo, Instant criadaEm, Instant atualizadaEm) {
        return new Carteira(donoId, saldo, criadaEm, atualizadaEm);
    }

    /** RN 5: o saldo precisa cobrir o total. */
    public Carteira debitar(BigDecimal valor, Instant agora) {
        exigirPositivo(valor);
        if (saldo.compareTo(valor) < 0) {
            throw new RegraDeNegocioException("Saldo insuficiente na carteira");
        }
        return new Carteira(donoId, saldo.subtract(valor), criadaEm, agora);
    }

    /** RN 7 e RN 9: valor liberado ao vendedor ou reembolsado ao comprador. */
    public Carteira creditar(BigDecimal valor, Instant agora) {
        exigirPositivo(valor);
        return new Carteira(donoId, saldo.add(valor), criadaEm, agora);
    }

    private static void exigirPositivo(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor movimentado deve ser maior que zero");
        }
    }

    public UUID donoId() { return donoId; }
    public BigDecimal saldo() { return saldo; }
    public Instant criadaEm() { return criadaEm; }
    public Instant atualizadaEm() { return atualizadaEm; }
}
