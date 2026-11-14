package com.g4.game_tracker_backend.apresentacao.marketplace.reputacao;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.marketplace.reputacao.ConsultarReputacao.PerfilDeVendedor;

/** notaMedia vem nula enquanto o usuário não tiver avaliações. */
public record PerfilVendedorResponse(UUID usuarioId, String nome, BigDecimal notaMedia, int totalAvaliacoes,
        int negociacoesBemSucedidas, List<AvaliacaoResponse> avaliacoes) {

    static PerfilVendedorResponse de(PerfilDeVendedor perfil) {
        return new PerfilVendedorResponse(perfil.usuarioId(), perfil.nome(), perfil.reputacao().notaMedia(),
                perfil.reputacao().totalAvaliacoes(), perfil.reputacao().negociacoesBemSucedidas(),
                perfil.avaliacoesRecebidas().stream()
                        .map(avaliacao -> AvaliacaoResponse.de(avaliacao, perfil.nomesDosAvaliadores()))
                        .toList());
    }
}
