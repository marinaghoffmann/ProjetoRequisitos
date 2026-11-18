package com.g4.game_tracker_backend.infraestrutura.persistencia.journal;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.journal.AnexoJournal;
import com.g4.game_tracker_backend.dominio.journal.AnexoJournalRepositorio;

import jakarta.persistence.EntityManager;

@Repository
@Transactional(readOnly = true)
class AnexoJournalRepositorioJpa implements AnexoJournalRepositorio {
    private final EntityManager entidades;

    AnexoJournalRepositorioJpa(EntityManager entidades) { this.entidades = entidades; }

    @Override
    @Transactional
    public void adicionar(UUID entradaId, AnexoJournal anexo, byte[] conteudo) {
        var entrada = entidades.find(EntradaDeJournalJpa.class, entradaId);
        var novo = new AnexoJournalJpa(entrada, anexo, conteudo);
        entrada.adicionarAnexo(novo);
        entidades.persist(novo);
    }

    @Override
    @Transactional
    public void remover(UUID entradaId, UUID anexoId) {
        entidades.find(EntradaDeJournalJpa.class, entradaId).removerAnexo(anexoId);
    }

    @Override
    public Optional<byte[]> conteudo(UUID entradaId, UUID anexoId) {
        return entidades.find(EntradaDeJournalJpa.class, entradaId).anexos().stream()
                .filter(anexo -> anexo.id().equals(anexoId)).findFirst().map(AnexoJournalJpa::conteudo);
    }
}
