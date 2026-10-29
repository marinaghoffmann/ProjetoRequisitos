package com.g4.game_tracker_backend.infraestrutura.persistencia.anuncios;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.FiltroAnuncios;

@Repository
public class AnuncioMarketplaceRepositorioPostgres implements AnuncioMarketplaceRepositorio {

    private final AnuncioMarketplaceSpringDataRepository springData;

    public AnuncioMarketplaceRepositorioPostgres(AnuncioMarketplaceSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public AnuncioMarketplace salvar(AnuncioMarketplace anuncio) {
        return springData.save(AnuncioMarketplaceJpa.deDominio(anuncio)).paraDominio();
    }

    @Override
    public Optional<AnuncioMarketplace> buscarPorId(UUID id) {
        return springData.findById(id).map(AnuncioMarketplaceJpa::paraDominio);
    }

    @Override
    public List<AnuncioMarketplace> buscarPublicos(FiltroAnuncios filtro) {
        return springData.buscarPublicos(FiltroAnuncios.statusVisiveis(), filtro.tiposAceitos(), filtro.jogoId(),
                vazioSeNulo(filtro.titulo()), vazioSeNulo(filtro.regiao()), filtro.precoMinimo(),
                filtro.precoMaximo(), filtro.estado()).stream().map(AnuncioMarketplaceJpa::paraDominio).toList();
    }

    @Override
    public List<AnuncioMarketplace> listarPorVendedor(UUID vendedorId) {
        return springData.findByVendedorIdOrderByAtualizadoEmDesc(vendedorId).stream()
                .map(AnuncioMarketplaceJpa::paraDominio).toList();
    }

    private static String vazioSeNulo(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
