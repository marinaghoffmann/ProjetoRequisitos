package com.g4.game_tracker_backend.aplicacao.marketplace.negociacao;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.compartilhado.ExecutorDeTransacao;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnuncioNegociavel;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnunciosDaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChatRepositorio;

/**
 * F-19 RN 1 e RN 2: o comprador abre a conversa a partir do anúncio. Se ela já existe, é reaberta (mesmo
 * que o anúncio não esteja mais ativo, para o comprador rever o histórico).
 */
public class IniciarConversa {

    private final ConversaChatRepositorio conversas;
    private final AnunciosDaNegociacao anuncios;
    private final VerificadorDeAcesso verificadorDeAcesso;
    private final ExecutorDeTransacao transacao;

    public IniciarConversa(ConversaChatRepositorio conversas, AnunciosDaNegociacao anuncios,
            VerificadorDeAcesso verificadorDeAcesso,
            ExecutorDeTransacao transacao) {
        this.conversas = conversas;
        this.anuncios = anuncios;
        this.verificadorDeAcesso = verificadorDeAcesso;
        this.transacao = transacao;
    }

    public ConversaChat executar(UUID anuncioId) {
        return transacao.executar(() -> {
            UUID compradorId = verificadorDeAcesso.usuarioAtualId();
            AnuncioNegociavel anuncio = AcessoAConversa.anuncio(anuncios, anuncioId);
            return conversas.buscarPorAnuncioEComprador(anuncioId, compradorId)
                    .orElseGet(() -> conversas.salvar(ConversaChat.iniciar(anuncio, compradorId, Instant.now())));
        });
    }
}
