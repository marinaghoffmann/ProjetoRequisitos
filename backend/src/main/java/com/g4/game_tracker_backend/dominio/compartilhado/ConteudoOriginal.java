package com.g4.game_tracker_backend.dominio.compartilhado;

public record ConteudoOriginal(String texto) implements IConteudoExibivel {
    @Override
    public String exibir() { return texto; }
}
