package com.g4.game_tracker_backend.infraestrutura.persistencia.journal;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournal;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournalRepositorio;
import com.g4.game_tracker_backend.dominio.journal.TipoEventoPlataforma;

@Repository
public class EntradaDeJournalRepositorioPostgres implements EntradaDeJournalRepositorio {

    private static final Sort MAIS_RECENTES_PRIMEIRO = Sort.by(Sort.Order.desc("dataSessao"),
            Sort.Order.desc("criadaEm"), Sort.Order.desc("id"));

    private final EntradaDeJournalSpringDataRepository springData;

    public EntradaDeJournalRepositorioPostgres(EntradaDeJournalSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public EntradaDeJournal salvar(EntradaDeJournal entrada) {
        springData.save(EntradaDeJournalJpa.deDominio(entrada));
        return entrada;
    }

    @Override
    public Optional<EntradaDeJournal> buscarPorId(UUID id) {
        return springData.findById(id).map(EntradaDeJournalJpa::paraDominio);
    }

    @Override
    public Optional<EntradaDeJournal> buscarPorOrigem(TipoEventoPlataforma evento, String origemId) {
        return springData.findByTipoEventoAndOrigemId(evento, origemId).map(EntradaDeJournalJpa::paraDominio);
    }

    @Override
    public Pagina<EntradaDeJournal> listarTimeline(UUID donoId, int pagina, int tamanho) {
        Page<EntradaDeJournalJpa> resultado = springData.findByDonoId(donoId,
                PageRequest.of(pagina - 1, tamanho, MAIS_RECENTES_PRIMEIRO));
        return new Pagina<>(resultado.map(EntradaDeJournalJpa::paraDominio).getContent(), pagina, tamanho,
                resultado.getTotalElements());
    }

    @Override
    public void remover(UUID id) {
        springData.deleteById(id);
    }
}
