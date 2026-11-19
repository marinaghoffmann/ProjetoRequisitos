package com.g4.game_tracker_backend.dominio.moderacao;

import java.util.Objects;

import com.g4.game_tracker_backend.dominio.compartilhado.IConteudoExibivel;

/** Decorator (F-14): troca o texto de um conteúdo removido pela mensagem de sistema. */
public final class ConteudoRemovidoDecorator implements IConteudoExibivel {

    public static final String MENSAGEM = "Esta postagem foi removida por violar as diretrizes";

    private final IConteudoExibivel original;
    private final boolean removido;

    public ConteudoRemovidoDecorator(IConteudoExibivel original, boolean removido) {
        this.original = Objects.requireNonNull(original);
        this.removido = removido;
    }

    @Override
    public String exibir() {
        return removido ? MENSAGEM : original.exibir();
    }
}
