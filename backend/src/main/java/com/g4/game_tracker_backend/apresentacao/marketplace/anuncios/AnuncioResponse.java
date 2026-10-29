package com.g4.game_tracker_backend.apresentacao.marketplace.anuncios;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.FotoAnuncio;

public record AnuncioResponse(UUID id, UUID vendedorId, UUID jogoId, String titulo, String estado, BigDecimal preco,
        String tipoNegociacao, String midia, String localidade, List<String> fotos, String status,
        Instant criadoEm, Instant atualizadoEm) {

    static AnuncioResponse de(AnuncioMarketplace anuncio) {
        return new AnuncioResponse(anuncio.id(), anuncio.vendedorId(), anuncio.jogoId(), anuncio.titulo(),
                anuncio.estado().name(), anuncio.preco(), anuncio.tipoNegociacao().name(), anuncio.midia().name(),
                anuncio.localidade(), anuncio.fotos().stream().map(FotoAnuncio::urlFoto).toList(),
                anuncio.status().name(), anuncio.criadoEm(), anuncio.atualizadoEm());
    }
}
