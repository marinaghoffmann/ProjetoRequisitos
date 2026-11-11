package com.g4.game_tracker_backend.infraestrutura.persistencia.pedidos;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.marketplace.pedidos.Carteira;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.CarteiraRepositorio;

@Repository
public class CarteiraRepositorioPostgres implements CarteiraRepositorio {

    private final CarteiraSpringDataRepository springData;

    public CarteiraRepositorioPostgres(CarteiraSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public Carteira salvar(Carteira carteira) {
        springData.save(CarteiraJpa.deDominio(carteira));
        return carteira;
    }

    @Override
    public Optional<Carteira> buscarPorDono(UUID donoId) {
        return springData.findById(donoId).map(CarteiraJpa::paraDominio);
    }

    @Override
    public Optional<Carteira> buscarPorDonoParaAtualizar(UUID donoId) {
        return springData.buscarParaAtualizar(donoId).map(CarteiraJpa::paraDominio);
    }
}
