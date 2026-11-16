package com.g4.game_tracker_backend.dominio.descoberta.similaridade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Agregado da similaridade entre dois jogos (F-10). É imutável: cada alteração devolve uma nova instância.
 *
 * RN 1: um jogo nunca é similar a ele mesmo, e a relação é bidirecional. O par é guardado uma vez só, com o
 * menor UUID em jogoA, e o grau vale para os dois sentidos.
 * RN 2: o grau efetivo (grauRelevancia) deriva do grauBase e dos feedbacks. Com mais de 15 feedbacks
 * NAO_RELEVANTE, a sugestão automática fica rebaixada e o grau cai pela metade; com 15 ou menos, nada
 * muda. Como o grauBase não é alterado, editar ou remover feedbacks desfaz o rebaixamento de forma
 * consistente.
 * RN 3: a origem MANUAL (administrador) sobrepõe a AUTOMATICA: o recálculo nunca altera uma conexão manual
 * nem recria uma conexão REMOVIDA pelo administrador.
 */
public final class SimilaridadeJogo {

    /** Pares automáticos só são guardados (e exibidos) com grau acima deste mínimo. */
    public static final BigDecimal GRAU_MINIMO = new BigDecimal("0.20");
    /** RN 2: acima deste número de feedbacks negativos, a sugestão é rebaixada. */
    public static final int LIMITE_DE_NEGATIVOS = 15;
    public static final BigDecimal FATOR_DE_REBAIXAMENTO = new BigDecimal("0.5");

    private final UUID id;
    private final UUID jogoAId;
    private final UUID jogoBId;
    private final BigDecimal grauBase;
    private final OrigemSimilaridade origem;
    private final StatusSimilaridade status;
    private final int totalFeedbacksUteis;
    private final int totalFeedbacksNegativos;
    private final Instant calculadaEm;
    private final Instant atualizadaEm;

    private SimilaridadeJogo(UUID id, UUID jogoUm, UUID jogoDois, BigDecimal grauBase, OrigemSimilaridade origem,
            StatusSimilaridade status, int totalFeedbacksUteis, int totalFeedbacksNegativos, Instant calculadaEm,
            Instant atualizadaEm) {
        this.id = Objects.requireNonNull(id, "O identificador da similaridade é obrigatório");
        Objects.requireNonNull(jogoUm, "Os dois jogos são obrigatórios");
        Objects.requireNonNull(jogoDois, "Os dois jogos são obrigatórios");
        if (jogoUm.equals(jogoDois)) {
            throw new RegraDeNegocioException("Um jogo não pode ser similar a ele mesmo");
        }
        boolean emOrdem = jogoUm.compareTo(jogoDois) < 0;
        this.jogoAId = emOrdem ? jogoUm : jogoDois;
        this.jogoBId = emOrdem ? jogoDois : jogoUm;
        this.grauBase = grau(grauBase);
        this.origem = Objects.requireNonNull(origem, "A origem da similaridade é obrigatória");
        this.status = Objects.requireNonNull(status, "O status da similaridade é obrigatório");
        this.totalFeedbacksUteis = Math.max(0, totalFeedbacksUteis);
        this.totalFeedbacksNegativos = Math.max(0, totalFeedbacksNegativos);
        this.calculadaEm = Objects.requireNonNull(calculadaEm, "A data do cálculo é obrigatória");
        this.atualizadaEm = Objects.requireNonNull(atualizadaEm, "A data de atualização é obrigatória");
    }

    public static SimilaridadeJogo automatica(UUID jogoUm, UUID jogoDois, BigDecimal grau, Instant agora) {
        return new SimilaridadeJogo(UUID.randomUUID(), jogoUm, jogoDois, grau, OrigemSimilaridade.AUTOMATICA,
                StatusSimilaridade.ATIVA, 0, 0, agora, agora);
    }

    public static SimilaridadeJogo manual(UUID jogoUm, UUID jogoDois, BigDecimal grau, Instant agora) {
        return new SimilaridadeJogo(UUID.randomUUID(), jogoUm, jogoDois, grau, OrigemSimilaridade.MANUAL,
                StatusSimilaridade.ATIVA, 0, 0, agora, agora);
    }

    public static SimilaridadeJogo reconstituir(UUID id, UUID jogoAId, UUID jogoBId, BigDecimal grauBase,
            OrigemSimilaridade origem, StatusSimilaridade status, int totalFeedbacksUteis,
            int totalFeedbacksNegativos, Instant calculadaEm, Instant atualizadaEm) {
        return new SimilaridadeJogo(id, jogoAId, jogoBId, grauBase, origem, status, totalFeedbacksUteis,
                totalFeedbacksNegativos, calculadaEm, atualizadaEm);
    }

    /** Ordem normalizada do par: (menor UUID, maior UUID). */
    public static UUID[] parNormalizado(UUID jogoUm, UUID jogoDois) {
        return jogoUm.compareTo(jogoDois) < 0 ? new UUID[] { jogoUm, jogoDois } : new UUID[] { jogoDois, jogoUm };
    }

    public static boolean acimaDoMinimo(BigDecimal grau) {
        return grau.compareTo(GRAU_MINIMO) > 0;
    }

    // ---- RN 3: recálculo e override manual ----

    /** O recálculo automático só altera conexões automáticas ativas. */
    public boolean aceitaRecalculo() {
        return origem == OrigemSimilaridade.AUTOMATICA && status == StatusSimilaridade.ATIVA;
    }

    public SimilaridadeJogo recalcular(BigDecimal novoGrau, Instant agora) {
        if (!aceitaRecalculo()) {
            return this;
        }
        return new SimilaridadeJogo(id, jogoAId, jogoBId, novoGrau, origem, status, totalFeedbacksUteis,
                totalFeedbacksNegativos, agora, agora);
    }

    /** O administrador fixa o grau: a conexão passa a MANUAL e ATIVA, mesmo que antes estivesse removida. */
    public SimilaridadeJogo definirManual(BigDecimal grau, Instant agora) {
        return new SimilaridadeJogo(id, jogoAId, jogoBId, grau, OrigemSimilaridade.MANUAL, StatusSimilaridade.ATIVA,
                totalFeedbacksUteis, totalFeedbacksNegativos, agora, agora);
    }

    /** O administrador remove a conexão; ela fica bloqueada para o recálculo automático. */
    public SimilaridadeJogo remover(Instant agora) {
        return new SimilaridadeJogo(id, jogoAId, jogoBId, grauBase, origem, StatusSimilaridade.REMOVIDA,
                totalFeedbacksUteis, totalFeedbacksNegativos, calculadaEm, agora);
    }

    // ---- RN 2: feedbacks ----

    /** Os totais vêm sempre da contagem dos feedbacks gravados, nunca de incrementos. */
    public SimilaridadeJogo comFeedbacks(int uteis, int negativos, Instant agora) {
        return new SimilaridadeJogo(id, jogoAId, jogoBId, grauBase, origem, status, uteis, negativos, calculadaEm,
                agora);
    }

    /** RN 2: mais de 15 negativos rebaixam a sugestão automática; a conexão manual é decisão do administrador. */
    public boolean rebaixada() {
        return origem == OrigemSimilaridade.AUTOMATICA && totalFeedbacksNegativos > LIMITE_DE_NEGATIVOS;
    }

    public BigDecimal grauRelevancia() {
        return rebaixada() ? grauBase.multiply(FATOR_DE_REBAIXAMENTO).setScale(2, RoundingMode.HALF_UP) : grauBase;
    }

    // ---- Consulta ----

    /** Aparece nas sugestões: ativa e, se automática, acima do grau mínimo. */
    public boolean visivel() {
        return status == StatusSimilaridade.ATIVA
                && (origem == OrigemSimilaridade.MANUAL || acimaDoMinimo(grauBase));
    }

    public boolean envolve(UUID jogoId) {
        return jogoAId.equals(jogoId) || jogoBId.equals(jogoId);
    }

    /** RN 1: a relação é bidirecional; a partir de um dos jogos, devolve o outro. */
    public UUID outroJogo(UUID jogoId) {
        if (!envolve(jogoId)) {
            throw new IllegalArgumentException("O jogo " + jogoId + " não faz parte desta similaridade");
        }
        return jogoAId.equals(jogoId) ? jogoBId : jogoAId;
    }

    private static BigDecimal grau(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0 || valor.compareTo(BigDecimal.ONE) > 0) {
            throw new RegraDeNegocioException("O grau de similaridade deve estar entre 0 e 1");
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public UUID id() { return id; }
    public UUID jogoAId() { return jogoAId; }
    public UUID jogoBId() { return jogoBId; }
    public BigDecimal grauBase() { return grauBase; }
    public OrigemSimilaridade origem() { return origem; }
    public StatusSimilaridade status() { return status; }
    public int totalFeedbacksUteis() { return totalFeedbacksUteis; }
    public int totalFeedbacksNegativos() { return totalFeedbacksNegativos; }
    public Instant calculadaEm() { return calculadaEm; }
    public Instant atualizadaEm() { return atualizadaEm; }
}
