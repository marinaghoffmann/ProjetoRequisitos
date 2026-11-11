package com.g4.game_tracker_backend.aplicacao.marketplace.pedidos;

import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.MudancaDeStatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;

/**
 * Transições do anúncio disparadas pelo pedido, sempre pelo ciclo de vida da F-18 e publicando o
 * AnuncioStatusAlterado na transação do caso de uso (a F-17 recalcula o volume de negociações com ele).
 */
@Component
class CicloDoAnuncioNoPedido {

    private final AnuncioMarketplaceRepositorio anuncios;
    private final ApplicationEventPublisher eventos;

    CicloDoAnuncioNoPedido(AnuncioMarketplaceRepositorio anuncios, ApplicationEventPublisher eventos) {
        this.anuncios = anuncios;
        this.eventos = eventos;
    }

    AnuncioMarketplace carregar(UUID anuncioId) {
        return anuncios.buscarPorId(anuncioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Anúncio não encontrado"));
    }

    /** RN 2: a compra direta reserva o anúncio (ATIVO -> EM_NEGOCIACAO). */
    void reservar(AnuncioMarketplace anuncio, Instant agora) {
        mudar(anuncio, StatusAnuncio.EM_NEGOCIACAO, agora);
    }

    /** RN 9: pedido cancelado devolve o anúncio à vitrine. */
    void liberar(UUID anuncioId, Instant agora) {
        AnuncioMarketplace anuncio = carregar(anuncioId);
        if (anuncio.status() == StatusAnuncio.EM_NEGOCIACAO) {
            mudar(anuncio, StatusAnuncio.ATIVO, agora);
        }
    }

    /** RN 11: pedido concluído marca o anúncio como VENDIDO. */
    void vender(UUID anuncioId, Instant agora) {
        mudar(carregar(anuncioId), StatusAnuncio.VENDIDO, agora);
    }

    private void mudar(AnuncioMarketplace anuncio, StatusAnuncio novo, Instant agora) {
        MudancaDeStatusAnuncio mudanca = anuncio.mudarStatus(novo, agora);
        anuncios.salvar(mudanca.anuncio());
        eventos.publishEvent(mudanca.evento());
    }
}
