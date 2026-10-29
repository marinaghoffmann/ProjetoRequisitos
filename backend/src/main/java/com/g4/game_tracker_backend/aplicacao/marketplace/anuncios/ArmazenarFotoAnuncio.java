package com.g4.game_tracker_backend.aplicacao.marketplace.anuncios;

import org.springframework.stereotype.Service;

import com.g4.game_tracker_backend.aplicacao.catalogo.ArmazenadorDeImagem;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;

/**
 * Envio das fotos reais do item (F-18 RN 1). Reaproveita o armazenamento de imagens do catálogo
 * (PNG, JPG ou WEBP, até 5 MB), mas libera o envio para qualquer usuário autenticado.
 */
@Service
public class ArmazenarFotoAnuncio {

    private final ArmazenadorDeImagem armazenador;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public ArmazenarFotoAnuncio(ArmazenadorDeImagem armazenador, VerificadorDeAcesso verificadorDeAcesso) {
        this.armazenador = armazenador;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    public String executar(byte[] conteudo, String nomeOriginal, String tipoConteudo) {
        verificadorDeAcesso.usuarioAtualId();
        return armazenador.armazenar(conteudo, nomeOriginal, tipoConteudo);
    }
}
