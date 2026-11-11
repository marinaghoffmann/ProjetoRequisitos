package com.g4.game_tracker_backend.dominio.marketplace.reputacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Reputação do usuário no marketplace (F-21 RN 3). Guarda a soma das estrelas, e não só a média, para
 * que o recálculo a cada avaliação seja exato e não acumule erro de arredondamento.
 */
public final class ReputacaoUsuario {

    private final UUID usuarioId;
    private final int somaEstrelas;
    private final int totalAvaliacoes;
    private final int negociacoesBemSucedidas;
    private final Instant atualizadaEm;

    private ReputacaoUsuario(UUID usuarioId, int somaEstrelas, int totalAvaliacoes, int negociacoesBemSucedidas,
            Instant atualizadaEm) {
        this.usuarioId = Objects.requireNonNull(usuarioId, "O usuário é obrigatório");
        if (somaEstrelas < 0 || totalAvaliacoes < 0 || negociacoesBemSucedidas < 0) {
            throw new IllegalArgumentException("Os totais da reputação não podem ser negativos");
        }
        this.somaEstrelas = somaEstrelas;
        this.totalAvaliacoes = totalAvaliacoes;
        this.negociacoesBemSucedidas = negociacoesBemSucedidas;
        this.atualizadaEm = atualizadaEm;
    }

    public static ReputacaoUsuario semAvaliacoes(UUID usuarioId) {
        return new ReputacaoUsuario(usuarioId, 0, 0, 0, null);
    }

    public static ReputacaoUsuario reconstituir(UUID usuarioId, int somaEstrelas, int totalAvaliacoes,
            int negociacoesBemSucedidas, Instant atualizadaEm) {
        return new ReputacaoUsuario(usuarioId, somaEstrelas, totalAvaliacoes, negociacoesBemSucedidas, atualizadaEm);
    }

    /** RN 3: a nova avaliação entra na média e conta mais uma negociação bem-sucedida. */
    public ReputacaoUsuario registrarAvaliacao(int estrelas, Instant agora) {
        return new ReputacaoUsuario(usuarioId, somaEstrelas + estrelas, totalAvaliacoes + 1,
                negociacoesBemSucedidas + 1, agora);
    }

    /** Média com duas casas, arredondada HALF_UP; nula enquanto não houver avaliações. */
    public BigDecimal notaMedia() {
        return totalAvaliacoes == 0 ? null
                : BigDecimal.valueOf(somaEstrelas).divide(BigDecimal.valueOf(totalAvaliacoes), 2, RoundingMode.HALF_UP);
    }

    public UUID usuarioId() { return usuarioId; }
    public int somaEstrelas() { return somaEstrelas; }
    public int totalAvaliacoes() { return totalAvaliacoes; }
    public int negociacoesBemSucedidas() { return negociacoesBemSucedidas; }
    public Instant atualizadaEm() { return atualizadaEm; }
}
