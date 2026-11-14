package com.g4.game_tracker_backend.aplicacao.marketplace.negociacao;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnuncioNegociavel;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnunciosDaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChatRepositorio;

/**
 * F-19 RN 1 e RN 2: o comprador abre a conversa a partir do anúncio. Se ela já existe, é reaberta (mesmo
 * que o anúncio não esteja mais ativo, para o comprador rever o histórico).
 */
@Service
public class IniciarConversa {

    private final ConversaChatRepositorio conversas;
    private final AnunciosDaNegociacao anuncios;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public IniciarConversa(ConversaChatRepositorio conversas, AnunciosDaNegociacao anuncios,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.conversas = conversas;
        this.anuncios = anuncios;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public ConversaChat executar(UUID anuncioId) {
        UUID compradorId = verificadorDeAcesso.usuarioAtualId();
        AnuncioNegociavel anuncio = AcessoAConversa.anuncio(anuncios, anuncioId);
        return conversas.buscarPorAnuncioEComprador(anuncioId, compradorId)
                .orElseGet(() -> conversas.salvar(ConversaChat.iniciar(anuncio, compradorId, Instant.now())));
    }
}
