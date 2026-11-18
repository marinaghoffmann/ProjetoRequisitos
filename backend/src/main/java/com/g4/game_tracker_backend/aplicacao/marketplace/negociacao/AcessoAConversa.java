package com.g4.game_tracker_backend.aplicacao.marketplace.negociacao;

import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnuncioNegociavel;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnunciosDaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChatRepositorio;

/** Carregamento com mensagem de "não encontrado" padronizada para os casos de uso da negociação. */
final class AcessoAConversa {

    private AcessoAConversa() {
    }

    /**
     * Recebe o {@link ConversasDoParticipante}: se a conversa existe mas o usuário não é parte dela, o proxy
     * recusa a leitura (RN 3) antes de chegar aqui.
     */
    static ConversaChat doParticipante(ConversaChatRepositorio conversas, UUID conversaId) {
        return conversas.buscarPorId(conversaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conversa não encontrada"));
    }

    static AnuncioNegociavel anuncio(AnunciosDaNegociacao anuncios, UUID anuncioId) {
        return anuncios.buscar(anuncioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Anúncio não encontrado"));
    }
}
