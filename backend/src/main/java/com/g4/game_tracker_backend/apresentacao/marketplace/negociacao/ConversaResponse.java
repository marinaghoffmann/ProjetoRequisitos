package com.g4.game_tracker_backend.apresentacao.marketplace.negociacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.ConsultarConversas.DetalheConversa;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;

/** Conversa aberta: dados do anúncio, as partes, as mensagens e as propostas em ordem cronológica. */
public record ConversaResponse(UUID id, UUID anuncioId, String tituloAnuncio, String fotoAnuncio,
        BigDecimal precoAnuncio, String tipoNegociacao, String statusAnuncio, UUID compradorId, String nomeComprador,
        UUID vendedorId, String nomeVendedor, boolean somenteLeitura, int naoLidas, Instant iniciadaEm,
        Instant ultimaAtividadeEm, List<MensagemResponse> mensagens, List<PropostaResponse> propostas) {

    static ConversaResponse de(DetalheConversa detalhe) {
        ConversaChat conversa = detalhe.conversa();
        return new ConversaResponse(conversa.id(), conversa.anuncioId(), detalhe.tituloAnuncio(),
                detalhe.fotoAnuncio(), detalhe.precoAnuncio(), detalhe.tipoNegociacao(),
                detalhe.statusAnuncio() == null ? null : detalhe.statusAnuncio().name(), conversa.compradorId(),
                detalhe.nomeComprador(), conversa.vendedorId(), detalhe.nomeVendedor(), detalhe.somenteLeitura(),
                detalhe.naoLidas(), conversa.iniciadaEm(), conversa.ultimaAtividadeEm(),
                conversa.mensagens().stream().map(MensagemResponse::de).toList(),
                conversa.propostas().stream().map(PropostaResponse::de).toList());
    }
}
