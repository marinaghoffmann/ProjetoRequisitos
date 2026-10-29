package com.g4.game_tracker_backend.dominio.marketplace.anuncios;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnuncioMarketplaceRepositorio {
    AnuncioMarketplace salvar(AnuncioMarketplace anuncio);
    Optional<AnuncioMarketplace> buscarPorId(UUID id);
    List<AnuncioMarketplace> buscarPublicos(FiltroAnuncios filtro);
    List<AnuncioMarketplace> listarPorVendedor(UUID vendedorId);
}
