package com.g4.game_tracker_backend.dominio.marketplace.negociacao;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.TipoNegociacao;

/** O que a negociação precisa saber do anúncio da F-18 (o AnuncioNegociavel do CML). */
public record AnuncioNegociavel(UUID anuncioId, UUID vendedorId, UUID jogoId, String titulo, BigDecimal preco,
        StatusAnuncio status, TipoNegociacao tipoNegociacao) {

    public AnuncioNegociavel {
        Objects.requireNonNull(anuncioId, "O anúncio é obrigatório");
        Objects.requireNonNull(vendedorId, "O vendedor é obrigatório");
        Objects.requireNonNull(status, "O status do anúncio é obrigatório");
        Objects.requireNonNull(tipoNegociacao, "O tipo de negociação é obrigatório");
    }

    /** RN 1: conversas novas e propostas só em anúncio ATIVO. */
    public boolean aceitaNegociacao() {
        return status.aceitaNegociacao();
    }

    /** RN 5: anúncio vendido ou cancelado deixa a conversa somente leitura. */
    public boolean encerrado() {
        return status == StatusAnuncio.VENDIDO || status == StatusAnuncio.CANCELADO;
    }

    public boolean aceitaProposta(TipoProposta tipo) {
        return switch (tipo) {
            case VALOR -> tipoNegociacao != TipoNegociacao.TROCA;
            case TROCA -> tipoNegociacao != TipoNegociacao.VENDA;
        };
    }
}
