package com.g4.game_tracker_backend.infraestrutura.persistencia.negociacao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChatRepositorio;

/**
 * As coleções da conversa são carregadas sob demanda na conversão para o domínio; por isso cada método roda
 * numa transação (a do caso de uso, quando já existe uma).
 */
@Repository
@Transactional
public class ConversaChatRepositorioPostgres implements ConversaChatRepositorio {

    private final ConversaChatSpringDataRepository springData;

    public ConversaChatRepositorioPostgres(ConversaChatSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public ConversaChat salvar(ConversaChat conversa) {
        springData.save(ConversaChatJpa.deDominio(conversa));
        return conversa;
    }

    @Override
    public Optional<ConversaChat> buscarPorId(UUID id) {
        return springData.findById(id).map(ConversaChatJpa::paraDominio);
    }

    @Override
    public Optional<ConversaChat> buscarPorAnuncioEComprador(UUID anuncioId, UUID compradorId) {
        return springData.findByAnuncioIdAndCompradorId(anuncioId, compradorId).map(ConversaChatJpa::paraDominio);
    }

    @Override
    public Optional<ConversaChat> buscarPorProposta(UUID propostaId) {
        return springData.buscarPorProposta(propostaId).map(ConversaChatJpa::paraDominio);
    }

    @Override
    public List<ConversaChat> listarPorParticipante(UUID usuarioId) {
        return springData.listarPorParticipante(usuarioId).stream().map(ConversaChatJpa::paraDominio).toList();
    }

    @Override
    public List<ConversaChat> listarPorAnuncio(UUID anuncioId) {
        return springData.findByAnuncioId(anuncioId).stream().map(ConversaChatJpa::paraDominio).toList();
    }

    @Override
    public List<ConversaChat> listarComReservaAceitaAte(Instant limite) {
        return springData.listarComReservaAceitaAte(limite).stream().map(ConversaChatJpa::paraDominio).toList();
    }
}
