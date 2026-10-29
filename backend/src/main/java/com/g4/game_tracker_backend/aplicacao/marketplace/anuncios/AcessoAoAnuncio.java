package com.g4.game_tracker_backend.aplicacao.marketplace.anuncios;

import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;

/** F-18 RN 2: busca o anúncio garantindo que o usuário autenticado é o proprietário. */
final class AcessoAoAnuncio {

    private AcessoAoAnuncio() {
    }

    static AnuncioMarketplace doProprietario(AnuncioMarketplaceRepositorio anuncios,
            VerificadorDeAcesso verificadorDeAcesso, UUID id) {
        AnuncioMarketplace anuncio = anuncios.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Anúncio não encontrado"));
        verificadorDeAcesso.exigirDono(anuncio.vendedorId(), "Apenas o proprietário pode alterar o anúncio");
        return anuncio;
    }
}
