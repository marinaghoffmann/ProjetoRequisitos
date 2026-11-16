package com.g4.game_tracker_backend.dominio.identidade;

public interface ArmazenamentoDeImagem {

    String salvar(String pasta, byte[] conteudo, String extensao);
}