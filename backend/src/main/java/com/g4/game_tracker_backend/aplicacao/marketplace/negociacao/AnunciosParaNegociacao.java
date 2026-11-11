package com.g4.game_tracker_backend.aplicacao.marketplace.negociacao;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.MudancaDeStatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnuncioNegociavel;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnunciosDaNegociacao;

/**
 * Implementa a porta da negociação sobre o agregado da F-18. Cada mudança passa pelo ciclo de vida do
 * anúncio e publica o AnuncioStatusAlterado na transação de quem chamou, como pede o contrato da T07.
 */
@Component
public class AnunciosParaNegociacao implements AnunciosDaNegociacao {

    private final AnuncioMarketplaceRepositorio anuncios;
    private final ApplicationEventPublisher eventos;

    public AnunciosParaNegociacao(AnuncioMarketplaceRepositorio anuncios, ApplicationEventPublisher eventos) {
        this.anuncios = anuncios;
        this.eventos = eventos;
    }

    @Override
    public Optional<AnuncioNegociavel> buscar(UUID anuncioId) {
        return anuncios.buscarPorId(anuncioId).map(AnunciosParaNegociacao::negociavel);
    }

    @Override
    public void reservar(UUID anuncioId) {
        mudar(anuncioId, StatusAnuncio.EM_NEGOCIACAO);
    }

    /** Só devolve à vitrine o que ainda está reservado; um anúncio já vendido não volta. */
    @Override
    public void liberar(UUID anuncioId) {
        AnuncioMarketplace anuncio = carregar(anuncioId);
        if (anuncio.status() == StatusAnuncio.EM_NEGOCIACAO) {
            mudar(anuncio, StatusAnuncio.ATIVO);
        }
    }

    static AnuncioNegociavel negociavel(AnuncioMarketplace anuncio) {
        return new AnuncioNegociavel(anuncio.id(), anuncio.vendedorId(), anuncio.jogoId(), anuncio.titulo(),
                anuncio.preco(), anuncio.status(), anuncio.tipoNegociacao());
    }

    private void mudar(UUID anuncioId, StatusAnuncio novo) {
        mudar(carregar(anuncioId), novo);
    }

    private void mudar(AnuncioMarketplace anuncio, StatusAnuncio novo) {
        MudancaDeStatusAnuncio mudanca = anuncio.mudarStatus(novo, Instant.now());
        anuncios.salvar(mudanca.anuncio());
        eventos.publishEvent(mudanca.evento());
    }

    private AnuncioMarketplace carregar(UUID anuncioId) {
        return anuncios.buscarPorId(anuncioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Anúncio não encontrado"));
    }
}
