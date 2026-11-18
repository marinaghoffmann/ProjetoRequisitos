package com.g4.game_tracker_backend.aplicacao.marketplace.negociacao;

import java.time.Instant;
import java.util.List;

import com.g4.game_tracker_backend.aplicacao.compartilhado.ExecutorDeTransacao;
import com.g4.game_tracker_backend.aplicacao.compartilhado.RotinaDePrazo;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnunciosDaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChatRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.PropostaNegociacao;

/**
 * F-19 RN 10, rotina agendada: a proposta aceita sem pedido há mais de 48 horas expira e o anúncio volta a
 * ATIVO. O laço, as transações e o tratamento de falha vêm do Template Method {@link RotinaDePrazo}.
 */
public class ExpirarReservasVencidas extends RotinaDePrazo<ConversaChat> {

    private final ConversaChatRepositorio conversas;
    private final AnunciosDaNegociacao anuncios;

    public ExpirarReservasVencidas(ConversaChatRepositorio conversas, AnunciosDaNegociacao anuncios,
            ExecutorDeTransacao transacao) {
        super(transacao);
        this.conversas = conversas;
        this.anuncios = anuncios;
    }

    @Override
    protected List<ConversaChat> vencidos(Instant agora) {
        return conversas.listarComReservaAceitaAte(agora.minus(PropostaNegociacao.PRAZO_PARA_PAGAR));
    }

    @Override
    protected boolean tratar(ConversaChat conversa, Instant agora) {
        ConversaChat atualizada = conversa.expirarReservaVencida(agora);
        if (atualizada == conversa) {
            return false;
        }
        conversas.salvar(atualizada);
        anuncios.liberar(conversa.anuncioId());
        return true;
    }

    @Override
    protected String descrever(ConversaChat conversa) {
        return "a reserva da conversa " + conversa.id();
    }
}
