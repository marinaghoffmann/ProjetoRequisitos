package com.g4.game_tracker_backend.dominio.marketplace.negociacao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversaChatRepositorio {

    ConversaChat salvar(ConversaChat conversa);

    Optional<ConversaChat> buscarPorId(UUID id);

    /** RN 2: no máximo uma conversa por anúncio e comprador (o vendedor é o dono do anúncio). */
    Optional<ConversaChat> buscarPorAnuncioEComprador(UUID anuncioId, UUID compradorId);

    Optional<ConversaChat> buscarPorProposta(UUID propostaId);

    /** Conversas em que o usuário é comprador ou vendedor, da atividade mais recente para a mais antiga. */
    List<ConversaChat> listarPorParticipante(UUID usuarioId);

    /** RN 9: as outras conversas do anúncio, para recusar as propostas pendentes. */
    List<ConversaChat> listarPorAnuncio(UUID anuncioId);

    /** RN 10: conversas com proposta aceita, sem pedido, respondida até o limite. */
    List<ConversaChat> listarComReservaAceitaAte(Instant limite);
}
