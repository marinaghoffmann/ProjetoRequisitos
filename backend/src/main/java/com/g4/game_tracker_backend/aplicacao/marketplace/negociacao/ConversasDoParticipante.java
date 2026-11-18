package com.g4.game_tracker_backend.aplicacao.marketplace.negociacao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChatRepositorio;

/**
 * Padrão Proxy (proxy de proteção) sobre o repositório de conversas: aplica a F-19 RN 3 em um só lugar.
 * Tem a mesma interface do repositório real, então os casos de uso do usuário não precisam repetir a
 * checagem. Uma conversa carregada por aqui só chega a quem é comprador ou vendedor dela; para os outros,
 * a leitura é recusada com {@link ConversaChat#RESTRITA}.
 *
 * <p>As rotinas do sistema (expiração de reservas, checkout) usam o repositório real, porque não agem em
 * nome de um participante.
 */
public class ConversasDoParticipante implements ConversaChatRepositorio {

    private final ConversaChatRepositorio real;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public ConversasDoParticipante(ConversaChatRepositorio real, VerificadorDeAcesso verificadorDeAcesso) {
        this.real = real;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    /** RN 3: só o comprador e o vendedor leem a conversa. */
    @Override
    public Optional<ConversaChat> buscarPorId(UUID id) {
        return real.buscarPorId(id).map(this::doUsuarioAtual);
    }

    /** RN 3: ninguém lista as conversas de outra pessoa. */
    @Override
    public List<ConversaChat> listarPorParticipante(UUID usuarioId) {
        if (!verificadorDeAcesso.usuarioAtualId().equals(usuarioId)) {
            throw new AcessoNegadoException(ConversaChat.RESTRITA);
        }
        return real.listarPorParticipante(usuarioId);
    }

    @Override
    public ConversaChat salvar(ConversaChat conversa) {
        return real.salvar(conversa);
    }

    @Override
    public Optional<ConversaChat> buscarPorAnuncioEComprador(UUID anuncioId, UUID compradorId) {
        return real.buscarPorAnuncioEComprador(anuncioId, compradorId);
    }

    @Override
    public Optional<ConversaChat> buscarPorProposta(UUID propostaId) {
        return real.buscarPorProposta(propostaId);
    }

    @Override
    public List<ConversaChat> listarPorAnuncio(UUID anuncioId) {
        return real.listarPorAnuncio(anuncioId);
    }

    @Override
    public List<ConversaChat> listarComReservaAceitaAte(Instant limite) {
        return real.listarComReservaAceitaAte(limite);
    }

    private ConversaChat doUsuarioAtual(ConversaChat conversa) {
        conversa.exigirParticipante(verificadorDeAcesso.usuarioAtualId());
        return conversa;
    }
}
