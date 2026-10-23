package com.g4.game_tracker_backend.dominio.compartilhado;

/** Lançada quando um agregado buscado por identificador não existe. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
