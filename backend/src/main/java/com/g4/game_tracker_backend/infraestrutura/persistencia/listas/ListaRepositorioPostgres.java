package com.g4.game_tracker_backend.infraestrutura.persistencia.listas;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaPersonalizada;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaRepositorio;

/**
 * Os itens são carregados sob demanda; cada método roda numa transação (ou entra na do caso de uso) para a
 * lista sair daqui já convertida, com os itens.
 */
@Repository
@Transactional(readOnly = true)
public class ListaRepositorioPostgres implements ListaRepositorio {

    private final ListaPersonalizadaSpringDataRepository springData;

    public ListaRepositorioPostgres(ListaPersonalizadaSpringDataRepository springData) {
        this.springData = springData;
    }

    /** A lista existente é alterada no lugar: os itens mantêm a identidade e a versão é conferida. */
    @Override
    @Transactional
    public ListaPersonalizada salvar(ListaPersonalizada lista) {
        ListaPersonalizadaJpa jpa = springData.findById(lista.id())
                .map(existente -> {
                    existente.atualizarCom(lista);
                    return existente;
                })
                .orElseGet(() -> ListaPersonalizadaJpa.deDominio(lista));
        springData.saveAndFlush(jpa);
        return lista;
    }

    @Override
    public Optional<ListaPersonalizada> buscarPorId(UUID id) {
        return springData.findById(id).map(ListaPersonalizadaJpa::paraDominio);
    }

    @Override
    public List<ListaPersonalizada> listarPorDono(UUID donoId) {
        return springData.findByDonoIdOrderByAtualizadaEmDescIdDesc(donoId).stream()
                .map(ListaPersonalizadaJpa::paraDominio)
                .toList();
    }

    @Override
    public boolean existePorDonoETituloIgnorandoCaixa(UUID donoId, String titulo, UUID listaIgnorada) {
        return springData.existeTitulo(donoId, ListaPersonalizada.chaveDoTitulo(titulo), listaIgnorada);
    }

    @Override
    @Transactional
    public void remover(UUID id) {
        springData.deleteById(id);
    }
}
