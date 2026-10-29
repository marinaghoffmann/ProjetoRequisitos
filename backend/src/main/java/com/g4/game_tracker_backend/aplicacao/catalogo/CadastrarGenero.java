package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.Genero;
import com.g4.game_tracker_backend.dominio.catalogo.GeneroRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.Papel;

@Service
public class CadastrarGenero {

    private final GeneroRepositorio generos;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public CadastrarGenero(GeneroRepositorio generos, VerificadorDeAcesso verificadorDeAcesso) {
        this.generos = generos;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public Genero executar(String nome) {
        verificadorDeAcesso.exigirPapel(Papel.ADMINISTRADOR);
        return generos.buscarPorNome(nome).orElseGet(() -> generos.salvar(Genero.criar(nome)));
    }

    @Transactional(readOnly = true)
    public List<Genero> listar() {
        return generos.listar();
    }
}
