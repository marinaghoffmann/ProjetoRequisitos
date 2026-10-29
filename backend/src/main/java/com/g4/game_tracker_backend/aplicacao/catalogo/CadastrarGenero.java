package com.g4.game_tracker_backend.aplicacao.catalogo;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.catalogo.Genero;
import com.g4.game_tracker_backend.dominio.catalogo.GeneroRepositorio;

@Service
public class CadastrarGenero {

    private final GeneroRepositorio generos;

    public CadastrarGenero(GeneroRepositorio generos) {
        this.generos = generos;
    }

    @Transactional
    public Genero executar(String nome) {
        return generos.buscarPorNome(nome).orElseGet(() -> generos.salvar(Genero.criar(nome)));
    }

    @Transactional(readOnly = true)
    public List<Genero> listar() {
        return generos.listar();
    }
}
