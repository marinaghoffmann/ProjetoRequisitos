package com.g4.game_tracker_backend.apresentacao.marketplace.negociacao;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.ConsultarConversas.ResumoConversa;

public record ResumoConversaResponse(UUID id, UUID anuncioId, String tituloAnuncio, String fotoAnuncio,
        String statusAnuncio, String papel, UUID outraParteId, String nomeOutraParte, String ultimaMensagem,
        String statusUltimaProposta, int naoLidas, Instant ultimaAtividadeEm) {

    static ResumoConversaResponse de(ResumoConversa resumo) {
        return new ResumoConversaResponse(resumo.conversaId(), resumo.anuncioId(), resumo.tituloAnuncio(),
                resumo.fotoAnuncio(), resumo.statusAnuncio() == null ? null : resumo.statusAnuncio().name(),
                resumo.papel(), resumo.outraParteId(), resumo.nomeOutraParte(), resumo.ultimaMensagem(),
                resumo.statusUltimaProposta() == null ? null : resumo.statusUltimaProposta().name(),
                resumo.naoLidas(), resumo.ultimaAtividadeEm());
    }
}
