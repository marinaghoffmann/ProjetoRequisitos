package com.g4.game_tracker_backend.aplicacao.marketplace.negociacao;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnuncioNegociavel;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnunciosDaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChatRepositorio;

/** RN 3: carrega a conversa garantindo que o usuário é o comprador ou o vendedor dela. */
final class AcessoAConversa {

    private AcessoAConversa() {
    }

    static ConversaChat doParticipante(ConversaChatRepositorio conversas, UUID conversaId, UUID usuarioId) {
        ConversaChat conversa = conversas.buscarPorId(conversaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conversa não encontrada"));
        conversa.exigirParticipante(usuarioId);
        return conversa;
    }

    static AnuncioNegociavel anuncio(AnunciosDaNegociacao anuncios, UUID anuncioId) {
        return anuncios.buscar(anuncioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Anúncio não encontrado"));
    }
}
