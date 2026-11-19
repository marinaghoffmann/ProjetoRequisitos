package com.g4.game_tracker_backend.dominio.biblioteca.listas;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListaRepositorio {
    ListaPersonalizada salvar(ListaPersonalizada lista);
    Optional<ListaPersonalizada> buscarPorId(UUID id);
    /** Listas do dono, da atualizada mais recentemente para a mais antiga, com os itens. */
    List<ListaPersonalizada> listarPorDono(UUID donoId);
    /**
     * RN 1: o dono já tem outra lista com o título, sem diferenciar maiúsculas, minúsculas e espaços nas
     * pontas. listaIgnorada (pode ser null) é a própria lista, na renomeação.
     */
    boolean existePorDonoETituloIgnorandoCaixa(UUID donoId, String titulo, UUID listaIgnorada);
    /** Busca global: listas cujo título contém o termo, sem diferenciar caixa, das mais recentes. */
    List<ListaPersonalizada> buscarPorTitulo(String termo, int limite);
    /** Exclusão física: a lista e os seus itens. */
    void remover(UUID id);
}
