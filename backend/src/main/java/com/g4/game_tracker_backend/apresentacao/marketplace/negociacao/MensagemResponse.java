package com.g4.game_tracker_backend.apresentacao.marketplace.negociacao;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.marketplace.negociacao.MensagemChat;

public record MensagemResponse(UUID id, UUID remetenteId, String texto, Instant enviadaEm, Instant lidaEm) {

    static MensagemResponse de(MensagemChat mensagem) {
        return new MensagemResponse(mensagem.id(), mensagem.remetenteId(), mensagem.texto(), mensagem.enviadaEm(),
                mensagem.lidaEm());
    }
}
