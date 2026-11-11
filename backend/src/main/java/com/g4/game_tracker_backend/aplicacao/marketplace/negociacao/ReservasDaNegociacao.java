package com.g4.game_tracker_backend.aplicacao.marketplace.negociacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChatRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.PropostaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.StatusProposta;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.TipoProposta;

/**
 * O que a negociação (F-19) oferece ao checkout (F-20): a reserva vigente do comprador, o registro do pedido
 * gerado a partir dela e a recusa das propostas pendentes quando o anúncio é comprado direto. Roda sempre
 * dentro da transação do checkout.
 */
@Service
@Transactional(propagation = Propagation.MANDATORY)
public class ReservasDaNegociacao {

    private final ConversaChatRepositorio conversas;
    private final GerenciarPropostas gerenciarPropostas;

    public ReservasDaNegociacao(ConversaChatRepositorio conversas, GerenciarPropostas gerenciarPropostas) {
        this.conversas = conversas;
        this.gerenciarPropostas = gerenciarPropostas;
    }

    /** F-20 RN 1: só a proposta ACEITA do próprio comprador, sem pedido e dentro das 48 horas (F-19 RN 10). */
    public ReservaAceita reservaDoComprador(UUID propostaId, UUID compradorId, Instant agora) {
        ConversaChat conversa = conversas.buscarPorProposta(propostaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Proposta não encontrada"));
        if (!conversa.compradorId().equals(compradorId)) {
            throw new AcessoNegadoException("Apenas o comprador da proposta pode gerar o pedido");
        }
        PropostaNegociacao proposta = conversa.proposta(propostaId);
        if (proposta.status() != StatusProposta.ACEITA || !proposta.reservaVigente()) {
            throw new RegraDeNegocioException("A proposta precisa estar aceita, e sem pedido, para gerar o pedido");
        }
        if (proposta.reservaVencida(agora)) {
            throw new RegraDeNegocioException("O prazo de 48 horas para pagar terminou");
        }
        return new ReservaAceita(conversa.id(), propostaId, conversa.anuncioId(), conversa.vendedorId(),
                proposta.tipo(), proposta.valorOferecido(), proposta.jogoOferecido(), proposta.prazoParaPagar());
    }

    /** F-19 RN 10: com o pedido gerado, a reserva não expira mais. */
    public void vincularPedido(UUID propostaId, UUID pedidoId) {
        ConversaChat conversa = conversas.buscarPorProposta(propostaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Proposta não encontrada"));
        conversas.salvar(conversa.vincularPedido(propostaId, pedidoId));
    }

    /** Compra direta reserva o anúncio: as propostas pendentes do anúncio perdem a vez (F-19 RN 9). */
    public void recusarPropostasPendentes(UUID anuncioId, Instant agora) {
        gerenciarPropostas.recusarOutrasConversas(anuncioId, null, agora);
    }

    /** valor vem nulo na troca: o item sai por R$ 0,00 e o comprador paga só o frete. */
    public record ReservaAceita(UUID conversaId, UUID propostaId, UUID anuncioId, UUID vendedorId, TipoProposta tipo,
            BigDecimal valor, String jogoOferecido, Instant prazoParaPagar) {

        public BigDecimal valorDoItem() {
            return tipo == TipoProposta.VALOR ? valor : BigDecimal.ZERO;
        }
    }
}
