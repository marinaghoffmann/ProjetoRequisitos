package com.g4.game_tracker_backend.aplicacao.catalogo;

import org.springframework.stereotype.Service;

@Service
public class ArmazenarImagem {

    private final ArmazenadorDeImagem armazenador;

    public ArmazenarImagem(ArmazenadorDeImagem armazenador) {
        this.armazenador = armazenador;
    }

    public String executar(byte[] conteudo, String nomeOriginal, String tipoConteudo) {
        return armazenador.armazenar(conteudo, nomeOriginal, tipoConteudo);
    }
}
