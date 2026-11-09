package com.g4.game_tracker_backend.infraestrutura.persistencia.journal;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournal;
import com.g4.game_tracker_backend.dominio.journal.TipoEventoPlataforma;
import com.g4.game_tracker_backend.dominio.journal.TipoOrigem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Entradas do journal, manuais e automáticas, na mesma tabela. A restrição única em (tipo_evento, origem_id)
 * garante uma entrada automática por ação de origem; nas manuais as duas colunas ficam nulas.
 */
@Entity
@Table(name = "journal_entradas", indexes = {
        @Index(name = "ix_journal_entradas_timeline", columnList = "dono_id, data_sessao")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_journal_entradas_origem", columnNames = { "tipo_evento", "origem_id" })
})
public class EntradaDeJournalJpa {

    @Id
    private UUID id;

    @Column(name = "dono_id", nullable = false)
    private UUID donoId;

    @Column(name = "jogo_id")
    private UUID jogoId;

    @Column(name = "data_sessao", nullable = false)
    private Instant dataSessao;

    @Column(nullable = false, length = EntradaDeJournal.LIMITE_DO_TEXTO)
    private String texto;

    @Column(name = "contem_spoiler", nullable = false)
    private boolean contemSpoiler;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_origem", nullable = false, length = 16)
    private TipoOrigem tipoOrigem;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", length = 24)
    private TipoEventoPlataforma tipoEvento;

    @Column(name = "origem_id", length = 64)
    private String origemId;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    @Column(name = "atualizada_em", nullable = false)
    private Instant atualizadaEm;

    protected EntradaDeJournalJpa() {
    }

    private EntradaDeJournalJpa(EntradaDeJournal entrada) {
        this.id = entrada.id();
        this.donoId = entrada.donoId();
        this.jogoId = entrada.jogoId();
        this.dataSessao = entrada.dataSessao();
        this.texto = entrada.texto();
        this.contemSpoiler = entrada.contemSpoiler();
        this.tipoOrigem = entrada.tipoOrigem();
        this.tipoEvento = entrada.tipoEvento();
        this.origemId = entrada.origemId();
        this.criadaEm = entrada.criadaEm();
        this.atualizadaEm = entrada.atualizadaEm();
    }

    static EntradaDeJournalJpa deDominio(EntradaDeJournal entrada) {
        return new EntradaDeJournalJpa(entrada);
    }

    EntradaDeJournal paraDominio() {
        return EntradaDeJournal.reconstituir(id, donoId, jogoId, dataSessao, texto, contemSpoiler, tipoOrigem,
                tipoEvento, origemId, criadaEm, atualizadaEm);
    }
}
