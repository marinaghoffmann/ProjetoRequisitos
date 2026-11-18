package com.g4.game_tracker_backend.infraestrutura.persistencia.journal;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournal;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournalRepositorio;
import com.g4.game_tracker_backend.dominio.journal.TipoEventoPlataforma;
import com.g4.game_tracker_backend.dominio.journal.FiltroJournal;

import jakarta.persistence.criteria.Predicate;

@Repository
@Transactional(readOnly = true)
public class EntradaDeJournalRepositorioPostgres implements EntradaDeJournalRepositorio {

    private static final Sort MAIS_RECENTES_PRIMEIRO = Sort.by(Sort.Order.desc("dataSessao"),
            Sort.Order.desc("criadaEm"), Sort.Order.desc("id"));

    private final EntradaDeJournalSpringDataRepository springData;

    public EntradaDeJournalRepositorioPostgres(EntradaDeJournalSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    @Transactional
    public EntradaDeJournal salvar(EntradaDeJournal entrada) {
        var atual = springData.findById(entrada.id()).orElse(null);
        if (atual == null) springData.save(EntradaDeJournalJpa.deDominio(entrada));
        else atual.atualizar(entrada);
        return entrada;
    }

    @Override
    public Optional<EntradaDeJournal> buscarParaAlterar(UUID id) {
        return springData.buscarParaAlterar(id).map(EntradaDeJournalJpa::paraDominio);
    }

    @Override
    public List<String> listarTags(UUID donoId) { return springData.listarTags(donoId); }

    @Override
    public Pagina<EntradaDeJournal> filtrar(UUID donoId, FiltroJournal filtro, int pagina, int tamanho) {
        Specification<EntradaDeJournalJpa> especificacao = (raiz, consulta, cb) -> {
            var condicoes = new ArrayList<Predicate>();
            condicoes.add(cb.equal(raiz.get("donoId"), donoId));
            if (filtro.jogoId() != null) condicoes.add(cb.equal(raiz.get("jogoId"), filtro.jogoId()));
            if (filtro.origem() != null) condicoes.add(cb.equal(raiz.get("tipoOrigem"), filtro.origem()));
            if (filtro.tag() != null) condicoes.add(cb.isMember(filtro.tag(), raiz.get("tags")));
            if (filtro.inicio() != null) condicoes.add(cb.greaterThanOrEqualTo(raiz.get("dataSessao"),
                    filtro.inicio().atStartOfDay(ZoneOffset.UTC).toInstant()));
            if (filtro.fim() != null && !filtro.fim().equals(LocalDate.MAX)) {
                condicoes.add(cb.lessThan(raiz.get("dataSessao"),
                        filtro.fim().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()));
            }
            return cb.and(condicoes.toArray(Predicate[]::new));
        };
        var resultado = springData.findAll(especificacao, PageRequest.of(pagina - 1, tamanho, MAIS_RECENTES_PRIMEIRO));
        return new Pagina<>(resultado.getContent().stream().map(EntradaDeJournalJpa::paraDominio).toList(),
                pagina, tamanho, resultado.getTotalElements());
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
    @Transactional
    public void remover(UUID id) {
        springData.deleteById(id);
    }
}
