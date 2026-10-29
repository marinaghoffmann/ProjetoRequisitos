package com.g4.game_tracker_backend.aplicacao.marketplace.anuncios;

import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.MudancaDeStatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;

/**
 * F-18 RN 2: o proprietário pausa, reativa ou exclui (cancela) o seu anúncio. EM_NEGOCIACAO e
 * VENDIDO são definidos pela negociação (F-19) e pelo pedido (F-20), não pelo proprietário.
 * O evento AnuncioStatusAlterado é publicado no contexto do Spring até o barramento da T04 existir.
 */
@Service
public class AlterarStatusAnuncio {

    private final AnuncioMarketplaceRepositorio anuncios;
    private final VerificadorDeAcesso verificadorDeAcesso;
    private final ApplicationEventPublisher eventos;

    public AlterarStatusAnuncio(AnuncioMarketplaceRepositorio anuncios, VerificadorDeAcesso verificadorDeAcesso,
            ApplicationEventPublisher eventos) {
        this.anuncios = anuncios;
        this.verificadorDeAcesso = verificadorDeAcesso;
        this.eventos = eventos;
    }

    @Transactional
    public AnuncioMarketplace executar(UUID id, StatusAnuncio novoStatus) {
        AnuncioMarketplace atual = AcessoAoAnuncio.doProprietario(anuncios, verificadorDeAcesso, id);
        if (!proprietarioPodeDefinir(atual.status(), novoStatus)) {
            throw new RegraDeNegocioException("O proprietário só pode pausar, reativar ou excluir o anúncio");
        }
        MudancaDeStatusAnuncio mudanca = atual.mudarStatus(novoStatus, Instant.now());
        AnuncioMarketplace salvo = anuncios.salvar(mudanca.anuncio());
        eventos.publishEvent(mudanca.evento());
        return salvo;
    }

    private boolean proprietarioPodeDefinir(StatusAnuncio atual, StatusAnuncio novo) {
        return switch (novo) {
            case PAUSADO, CANCELADO -> true;
            case ATIVO -> atual != StatusAnuncio.EM_NEGOCIACAO;
            case EM_NEGOCIACAO, VENDIDO -> false;
        };
    }
}
