package com.g4.game_tracker_backend.aplicacao.biblioteca.listas;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaPersonalizada;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaRepositorio;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

/**
 * F-04: o usuário autenticado cria, renomeia (título e descrição) e exclui as suas listas.
 *
 * RN 1: o título é único entre as listas do dono, sem diferenciar maiúsculas e minúsculas, também ao
 * renomear. A exclusão é física e leva os itens; só o dono altera ou exclui (checado no agregado).
 */
@CasoDeUso
public class GerenciarListas {

    private final ListaRepositorio listas;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public GerenciarListas(ListaRepositorio listas, VerificadorDeAcesso verificadorDeAcesso) {
        this.listas = listas;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transacional
    public ListaPersonalizada criar(String titulo, String descricao) {
        UUID donoId = verificadorDeAcesso.usuarioAtualId();
        ListaPersonalizada nova = ListaPersonalizada.criar(donoId, titulo, descricao, Instant.now());
        ListasDoUsuario.exigirTituloLivre(listas, donoId, nova.titulo(), null);
        return listas.salvar(nova);
    }

    @Transacional
    public ListaPersonalizada editar(UUID listaId, String titulo, String descricao) {
        ListaPersonalizada atual = ListasDoUsuario.existente(listas, listaId);
        ListaPersonalizada editada = atual.editar(verificadorDeAcesso.usuarioAtualId(), titulo, descricao,
                Instant.now());
        ListasDoUsuario.exigirTituloLivre(listas, editada.donoId(), editada.titulo(), editada.id());
        return listas.salvar(editada);
    }

    @Transacional
    public void excluir(UUID listaId) {
        ListaPersonalizada atual = ListasDoUsuario.existente(listas, listaId);
        atual.exigirDono(verificadorDeAcesso.usuarioAtualId());
        listas.remover(atual.id());
    }
}
