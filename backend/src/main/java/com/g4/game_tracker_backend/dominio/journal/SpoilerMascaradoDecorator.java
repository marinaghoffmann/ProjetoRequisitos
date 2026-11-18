package com.g4.game_tracker_backend.dominio.journal;

import java.util.Objects;

public final class SpoilerMascaradoDecorator implements IConteudoExibivel {
    private final IConteudoExibivel original;
    private final boolean revelado;

    public SpoilerMascaradoDecorator(IConteudoExibivel original, boolean revelado) {
        this.original = Objects.requireNonNull(original);
        this.revelado = revelado;
    }

    @Override
    public String exibir() { return revelado ? original.exibir() : null; }
}
