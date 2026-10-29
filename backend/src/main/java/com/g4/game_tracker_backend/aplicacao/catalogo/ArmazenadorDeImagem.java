package com.g4.game_tracker_backend.aplicacao.catalogo;

public interface ArmazenadorDeImagem {
    String armazenar(byte[] conteudo, String nomeOriginal, String tipoConteudo);
}
