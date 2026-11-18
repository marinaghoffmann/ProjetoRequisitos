package com.g4.game_tracker_backend.infraestrutura.persistencia.journal;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.journal.AnexoJournal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "journal_anexos")
class AnexoJournalJpa {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entrada_id", nullable = false)
    private EntradaDeJournalJpa entrada;
    @Column(nullable = false, length = 120)
    private String nomeArquivo;
    @Column(nullable = false, length = 24)
    private String tipoMidia;
    @Column(nullable = false)
    private long tamanho;
    // BYTEA existe no PostgreSQL e no H2 em modo PostgreSQL; evita o OID das APIs JDBC LOB.
    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] conteudo;

    protected AnexoJournalJpa() { }

    AnexoJournalJpa(EntradaDeJournalJpa entrada, AnexoJournal anexo, byte[] conteudo) {
        this.entrada = entrada;
        this.id = anexo.id();
        this.nomeArquivo = anexo.nomeArquivo();
        this.tipoMidia = anexo.tipoMidia();
        this.tamanho = anexo.tamanho();
        this.conteudo = conteudo.clone();
    }

    UUID id() { return id; }
    byte[] conteudo() { return conteudo.clone(); }
    AnexoJournal paraDominio() { return new AnexoJournal(id, nomeArquivo, tipoMidia, tamanho); }
}
