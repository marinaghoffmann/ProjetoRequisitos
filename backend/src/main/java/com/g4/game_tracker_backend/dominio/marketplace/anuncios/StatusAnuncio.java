package com.g4.game_tracker_backend.dominio.marketplace.anuncios;

import java.util.Set;

/**
 * Ciclo de vida do anúncio (F-18 RN 2). As transições permitidas fazem parte do contrato
 * combinado entre anúncios (F-18), negociação (F-19) e pedidos (F-20):
 *
 * <pre>
 * ATIVO          -> PAUSADO, EM_NEGOCIACAO, CANCELADO
 * PAUSADO        -> ATIVO, CANCELADO
 * EM_NEGOCIACAO  -> ATIVO (reserva liberada), VENDIDO (pedido concluído)
 * VENDIDO, CANCELADO: finais
 * </pre>
 */
public enum StatusAnuncio {
    ATIVO,
    PAUSADO,
    EM_NEGOCIACAO,
    VENDIDO,
    CANCELADO;

    public Set<StatusAnuncio> proximosPermitidos() {
        return switch (this) {
            case ATIVO -> Set.of(PAUSADO, EM_NEGOCIACAO, CANCELADO);
            case PAUSADO -> Set.of(ATIVO, CANCELADO);
            case EM_NEGOCIACAO -> Set.of(ATIVO, VENDIDO);
            case VENDIDO, CANCELADO -> Set.of();
        };
    }

    public boolean podeMudarPara(StatusAnuncio novo) {
        return proximosPermitidos().contains(novo);
    }

    public boolean finalizado() {
        return proximosPermitidos().isEmpty();
    }

    /** F-18 RN 3: anúncios vendidos (e cancelados) não aparecem na busca pública. */
    public boolean visivelNaBuscaPublica() {
        return this == ATIVO || this == EM_NEGOCIACAO;
    }

    /** F-19 RN 1: só um anúncio ativo recebe novas conversas e propostas. */
    public boolean aceitaNegociacao() {
        return this == ATIVO;
    }
}
