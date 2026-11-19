package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.List;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.Genero;
import com.g4.game_tracker_backend.dominio.catalogo.GeneroRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

@CasoDeUso
public class CadastrarGenero {

    private final GeneroRepositorio generos;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public CadastrarGenero(GeneroRepositorio generos, VerificadorDeAcesso verificadorDeAcesso) {
        this.generos = generos;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transacional
    public Genero executar(String nome) {
        verificadorDeAcesso.exigirPapel(Papel.ADMINISTRADOR);
        return generos.buscarPorNome(nome).orElseGet(() -> generos.salvar(Genero.criar(nome)));
    }

    @Transacional(somenteLeitura = true)
    public List<Genero> listar() {
        return generos.listar();
    }
}
