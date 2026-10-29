package com.g4.game_tracker_backend.aplicacao.catalogo;

import org.springframework.stereotype.Service;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.identidade.Papel;

@Service
public class ArmazenarImagem {

    private final ArmazenadorDeImagem armazenador;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public ArmazenarImagem(ArmazenadorDeImagem armazenador, VerificadorDeAcesso verificadorDeAcesso) {
        this.armazenador = armazenador;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    public String executar(byte[] conteudo, String nomeOriginal, String tipoConteudo) {
        verificadorDeAcesso.exigirPapel(Papel.ADMINISTRADOR, Papel.DESENVOLVEDOR);
        return armazenador.armazenar(conteudo, nomeOriginal, tipoConteudo);
    }
}
