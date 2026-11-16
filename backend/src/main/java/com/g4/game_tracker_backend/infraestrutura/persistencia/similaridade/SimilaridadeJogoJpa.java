package com.g4.game_tracker_backend.infraestrutura.persistencia.similaridade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.descoberta.similaridade.OrigemSimilaridade;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeJogo;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.StatusSimilaridade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

/**
 * Uma linha por par de jogos (RN 1): jogo_a_id é sempre o menor UUID, e a restrição única impede o par
 * duplicado. As consultas olham os dois lados. grau_base é o grau calculado ou definido pelo administrador; o
 * grau efetivo e o rebaixamento (RN 2) são derivados no domínio, e rebaixada fica gravada para consulta.
 * A versão (bloqueio otimista) evita perder contagens de feedbacks gravados ao mesmo tempo.
 */
@Entity
@Table(name = "similaridades_jogos", uniqueConstraints = @UniqueConstraint(name = "uk_similaridades_par",
        columnNames = { "jogo_a_id", "jogo_b_id" }), indexes = {
                @Index(name = "ix_similaridades_jogo_a", columnList = "jogo_a_id"),
                @Index(name = "ix_similaridades_jogo_b", columnList = "jogo_b_id") })
public class SimilaridadeJogoJpa {

    @Id
    private UUID id;

    /** null enquanto não foi gravada: assim o Spring Data a persiste como nova. */
    @Version
    private Long versao;

    @Column(name = "jogo_a_id", nullable = false)
    private UUID jogoAId;

    @Column(name = "jogo_b_id", nullable = false)
    private UUID jogoBId;

    @Column(name = "grau_base", nullable = false, precision = 3, scale = 2)
    private BigDecimal grauBase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrigemSimilaridade origem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StatusSimilaridade status;

    @Column(name = "total_feedbacks_uteis", nullable = false)
    private int totalFeedbacksUteis;

    @Column(name = "total_feedbacks_negativos", nullable = false)
    private int totalFeedbacksNegativos;

    @Column(nullable = false)
    private boolean rebaixada;

    @Column(name = "calculada_em", nullable = false)
    private Instant calculadaEm;

    @Column(name = "atualizada_em", nullable = false)
    private Instant atualizadaEm;

    protected SimilaridadeJogoJpa() {
    }

    private SimilaridadeJogoJpa(SimilaridadeJogo similaridade) {
        this.id = similaridade.id();
        this.jogoAId = similaridade.jogoAId();
        this.jogoBId = similaridade.jogoBId();
        atualizarCom(similaridade);
    }

    static SimilaridadeJogoJpa deDominio(SimilaridadeJogo similaridade) {
        return new SimilaridadeJogoJpa(similaridade);
    }

    void atualizarCom(SimilaridadeJogo similaridade) {
        this.grauBase = similaridade.grauBase();
        this.origem = similaridade.origem();
        this.status = similaridade.status();
        this.totalFeedbacksUteis = similaridade.totalFeedbacksUteis();
        this.totalFeedbacksNegativos = similaridade.totalFeedbacksNegativos();
        this.rebaixada = similaridade.rebaixada();
        this.calculadaEm = similaridade.calculadaEm();
        this.atualizadaEm = similaridade.atualizadaEm();
    }

    SimilaridadeJogo paraDominio() {
        return SimilaridadeJogo.reconstituir(id, jogoAId, jogoBId, grauBase, origem, status, totalFeedbacksUteis,
                totalFeedbacksNegativos, calculadaEm, atualizadaEm);
    }
}
