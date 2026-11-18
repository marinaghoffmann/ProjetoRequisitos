package com.g4.game_tracker_backend.infraestrutura.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.g4.game_tracker_backend.aplicacao.compartilhado.ExecutorDeTransacao;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.AnunciosParaNegociacao;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.ConsultarConversas;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.ConversasDoParticipante;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.EnviarMensagem;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.ExpirarReservasVencidas;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.GerenciarPropostas;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.IniciarConversa;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.ReservasDaNegociacao;
import com.g4.game_tracker_backend.dominio.compartilhado.PublicadorDeEventos;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnunciosDaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChatRepositorio;

/**
 * Wiring do contexto de negociação (F-19). Os casos de uso são Java puro; aqui viram beans do Spring.
 *
 * <p>Os casos de uso que agem em nome do usuário recebem o {@link ConversasDoParticipante} (proxy de proteção
 * da RN 3) no lugar do repositório real. As rotinas do sistema e a reserva usada pelo checkout recebem o real.
 */
@Configuration
public class NegociacaoConfig {

    @Bean
    AnunciosParaNegociacao anunciosParaNegociacao(AnuncioMarketplaceRepositorio anuncios,
            PublicadorDeEventos eventos) {
        return new AnunciosParaNegociacao(anuncios, eventos);
    }

    @Bean
    IniciarConversa iniciarConversa(ConversaChatRepositorio conversas, AnunciosDaNegociacao anuncios,
            VerificadorDeAcesso verificadorDeAcesso, ExecutorDeTransacao transacao,
            com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico moderacao) {
        return new IniciarConversa(doParticipante(conversas, verificadorDeAcesso),
                anuncios, verificadorDeAcesso, transacao, moderacao);
    }

    @Bean
    EnviarMensagem enviarMensagem(ConversaChatRepositorio conversas, AnunciosDaNegociacao anuncios,
            VerificadorDeAcesso verificadorDeAcesso, ExecutorDeTransacao transacao) {
        return new EnviarMensagem(doParticipante(conversas, verificadorDeAcesso),
                anuncios, verificadorDeAcesso, transacao);
    }

    @Bean
    GerenciarPropostas gerenciarPropostas(ConversaChatRepositorio conversas, AnunciosDaNegociacao anuncios,
            VerificadorDeAcesso verificadorDeAcesso, ExecutorDeTransacao transacao,
            com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico moderacao) {
        return new GerenciarPropostas(doParticipante(conversas, verificadorDeAcesso),
                anuncios, verificadorDeAcesso, transacao, moderacao);
    }

    @Bean
    ConsultarConversas consultarConversas(ConversaChatRepositorio conversas, AnuncioMarketplaceRepositorio anuncios,
            UsuarioRepositorio usuarios, VerificadorDeAcesso verificadorDeAcesso, ExecutorDeTransacao transacao) {
        return new ConsultarConversas(doParticipante(conversas, verificadorDeAcesso),
                anuncios, usuarios, verificadorDeAcesso, transacao);
    }

    @Bean
    ReservasDaNegociacao reservasDaNegociacao(ConversaChatRepositorio conversas,
            GerenciarPropostas gerenciarPropostas, ExecutorDeTransacao transacao) {
        return new ReservasDaNegociacao(conversas, gerenciarPropostas, transacao);
    }

    @Bean
    ExpirarReservasVencidas expirarReservasVencidas(ConversaChatRepositorio conversas,
            AnunciosDaNegociacao anuncios, ExecutorDeTransacao transacao) {
        return new ExpirarReservasVencidas(conversas, anuncios, transacao);
    }

    /**
     * Não é bean: tem o mesmo tipo do repositório real, e o Spring não saberia qual injetar nos outros pontos.
     */
    private static ConversasDoParticipante doParticipante(ConversaChatRepositorio conversas,
            VerificadorDeAcesso verificadorDeAcesso) {
        return new ConversasDoParticipante(conversas, verificadorDeAcesso);
    }
}
