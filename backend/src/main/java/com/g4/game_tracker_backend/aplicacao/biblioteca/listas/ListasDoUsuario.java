package com.g4.game_tracker_backend.aplicacao.biblioteca.listas;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaPersonalizada;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/** Buscas e checagens compartilhadas pelos casos de uso da F-04. */
final class ListasDoUsuario {

    private ListasDoUsuario() {
    }

    static ListaPersonalizada existente(ListaRepositorio listas, UUID listaId) {
        return listas.buscarPorId(listaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lista não encontrada"));
    }

    /** RN 1: título único por dono, ignorando maiúsculas, minúsculas e espaços nas pontas. */
    static void exigirTituloLivre(ListaRepositorio listas, UUID donoId, String titulo, UUID listaIgnorada) {
        if (listas.existePorDonoETituloIgnorandoCaixa(donoId, titulo, listaIgnorada)) {
            throw new RegraDeNegocioException(ListaPersonalizada.TITULO_REPETIDO);
        }
    }
}
