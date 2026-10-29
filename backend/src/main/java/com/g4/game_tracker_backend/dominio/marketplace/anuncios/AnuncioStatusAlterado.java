package com.g4.game_tracker_backend.dominio.marketplace.anuncios;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de domínio emitido a cada mudança de status de um anúncio. É o contrato que F-17
 * (volume de negociações), F-16 (journal do comprador, quando VENDIDO) e F-19/F-20 consomem.
 * A publicação na mesma transação fica a cargo do barramento de eventos (T04).
 */
public record AnuncioStatusAlterado(
        UUID anuncioId,
        UUID jogoId,
        UUID vendedorId,
        StatusAnuncio statusAnterior,
        StatusAnuncio novoStatus,
        Instant ocorridoEm) {

    public AnuncioStatusAlterado {
        Objects.requireNonNull(anuncioId, "O anúncio é obrigatório");
        Objects.requireNonNull(jogoId, "O jogo é obrigatório");
        Objects.requireNonNull(vendedorId, "O vendedor é obrigatório");
        Objects.requireNonNull(novoStatus, "O novo status é obrigatório");
        Objects.requireNonNull(ocorridoEm, "O instante da mudança é obrigatório");
    }

    /** Equivale ao AnuncioVendidoEvent do modelo CML. */
    public boolean vendido() {
        return novoStatus == StatusAnuncio.VENDIDO;
    }
}
