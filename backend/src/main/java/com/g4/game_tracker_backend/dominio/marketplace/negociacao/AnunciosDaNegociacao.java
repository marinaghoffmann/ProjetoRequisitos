package com.g4.game_tracker_backend.dominio.marketplace.negociacao;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta ACL para o contexto de anúncios (o IAnuncioGateway do CML). As mudanças de status passam pelo
 * ciclo de vida do anúncio e publicam o AnuncioStatusAlterado, como pede o contrato da T07.
 */
public interface AnunciosDaNegociacao {

    Optional<AnuncioNegociavel> buscar(UUID anuncioId);

    /** RN 9: ATIVO -> EM_NEGOCIACAO. */
    void reservar(UUID anuncioId);

    /** RN 10: EM_NEGOCIACAO -> ATIVO. */
    void liberar(UUID anuncioId);
}
