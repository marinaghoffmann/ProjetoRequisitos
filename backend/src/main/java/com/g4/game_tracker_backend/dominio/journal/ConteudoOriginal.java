package com.g4.game_tracker_backend.dominio.journal;

public record ConteudoOriginal(String texto) implements IConteudoExibivel {
    @Override
    public String exibir() { return texto; }
}
