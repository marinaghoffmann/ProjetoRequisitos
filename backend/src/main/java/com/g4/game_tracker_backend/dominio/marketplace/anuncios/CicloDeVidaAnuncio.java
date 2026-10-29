package com.g4.game_tracker_backend.dominio.marketplace.anuncios;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Regra única de transição de status: o agregado AnuncioMarketplace (F-18) delega a ela e
 * recebe de volta o evento que deve ser publicado.
 */
public final class CicloDeVidaAnuncio {

    private CicloDeVidaAnuncio() {
    }

    public static AnuncioStatusAlterado transicionar(UUID anuncioId, UUID jogoId, UUID vendedorId,
            StatusAnuncio atual, StatusAnuncio novo, Instant agora) {
        if (atual == null || !atual.podeMudarPara(novo)) {
            throw new RegraDeNegocioException(
                    "O anúncio não pode passar de " + atual + " para " + novo);
        }
        return new AnuncioStatusAlterado(anuncioId, jogoId, vendedorId, atual, novo, agora);
    }
}
