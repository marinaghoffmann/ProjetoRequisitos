package com.g4.game_tracker_backend.infraestrutura.persistencia.catalogo;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;

@Repository
public class DesenvolvedoraRepositorioPostgres implements DesenvolvedoraRepositorio {

    private final DesenvolvedoraSpringDataRepository springData;

    public DesenvolvedoraRepositorioPostgres(DesenvolvedoraSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public Desenvolvedora salvar(Desenvolvedora desenvolvedora) {
        return springData.save(DesenvolvedoraJpa.deDominio(desenvolvedora)).paraDominio();
    }

    @Override
    public Optional<Desenvolvedora> buscarPorId(UUID id) {
        return springData.findById(id).map(DesenvolvedoraJpa::paraDominio);
    }

    @Override
    public Optional<Desenvolvedora> buscarPorNome(String nome) {
        return springData.findByNomeIgnoreCase(nome.trim()).map(DesenvolvedoraJpa::paraDominio);
    }

    @Override
    public List<Desenvolvedora> listar() {
        return springData.findAll().stream().map(DesenvolvedoraJpa::paraDominio).toList();
    }
}
