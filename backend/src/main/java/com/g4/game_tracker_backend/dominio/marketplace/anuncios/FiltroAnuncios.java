package com.g4.game_tracker_backend.dominio.marketplace.anuncios;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Filtros da busca pública do marketplace (consulta da F-18). Todos são opcionais.
 * Buscar por VENDA ou TROCA também traz os anúncios que aceitam AMBOS.
 */
public record FiltroAnuncios(UUID jogoId, String titulo, BigDecimal precoMinimo, BigDecimal precoMaximo,
        EstadoConservacao estado, TipoNegociacao tipoNegociacao, String regiao) {

    public static FiltroAnuncios todos() {
        return new FiltroAnuncios(null, null, null, null, null, null, null);
    }

    public Set<TipoNegociacao> tiposAceitos() {
        if (tipoNegociacao == null) {
            return EnumSet.allOf(TipoNegociacao.class);
        }
        return tipoNegociacao == TipoNegociacao.AMBOS
                ? EnumSet.of(TipoNegociacao.AMBOS)
                : EnumSet.of(tipoNegociacao, TipoNegociacao.AMBOS);
    }

    /** F-18 RN 3: anúncios vendidos (e cancelados ou pausados) ficam fora da busca pública. */
    public static Set<StatusAnuncio> statusVisiveis() {
        EnumSet<StatusAnuncio> visiveis = EnumSet.noneOf(StatusAnuncio.class);
        for (StatusAnuncio status : StatusAnuncio.values()) {
            if (status.visivelNaBuscaPublica()) {
                visiveis.add(status);
            }
        }
        return visiveis;
    }
}
