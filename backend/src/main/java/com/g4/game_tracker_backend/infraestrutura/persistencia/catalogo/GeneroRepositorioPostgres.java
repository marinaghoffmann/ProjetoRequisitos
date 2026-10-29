package com.g4.game_tracker_backend.infraestrutura.persistencia.catalogo;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.catalogo.Genero;
import com.g4.game_tracker_backend.dominio.catalogo.GeneroRepositorio;

@Repository
public class GeneroRepositorioPostgres implements GeneroRepositorio {

    private final GeneroSpringDataRepository springData;

    public GeneroRepositorioPostgres(GeneroSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public Genero salvar(Genero genero) {
        return springData.save(GeneroJpa.deDominio(genero)).paraDominio();
    }

    @Override
    public Optional<Genero> buscarPorId(UUID id) {
        return springData.findById(id).map(GeneroJpa::paraDominio);
    }

    @Override
    public Optional<Genero> buscarPorNome(String nome) {
        return springData.findByNomeIgnoreCase(nome.trim()).map(GeneroJpa::paraDominio);
    }

    @Override
    public List<Genero> listar() {
        return springData.findAll().stream().map(GeneroJpa::paraDominio).toList();
    }
}
