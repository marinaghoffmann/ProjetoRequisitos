package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.moderacao.Advertencia;
import com.g4.game_tracker_backend.dominio.moderacao.AdvertenciaRepositorio;

@Repository
class AdvertenciaRepositorioPostgres implements AdvertenciaRepositorio {

    private final AdvertenciaRepository springData;

    AdvertenciaRepositorioPostgres(AdvertenciaRepository springData) {
        this.springData = springData;
    }

    @Override
    public Advertencia salvar(Advertencia advertencia) {
        return springData.save(AdvertenciaJpa.deDominio(advertencia)).paraDominio();
    }

    @Override
    public List<Advertencia> listarPorUsuario(UUID usuarioId) {
        return springData.findByUsuarioIdOrderByRegistradaEmDesc(usuarioId).stream()
                .map(AdvertenciaJpa::paraDominio).toList();
    }

    @Override
    public long contarPorUsuario(UUID usuarioId) {
        return springData.countByUsuarioId(usuarioId);
    }
}
