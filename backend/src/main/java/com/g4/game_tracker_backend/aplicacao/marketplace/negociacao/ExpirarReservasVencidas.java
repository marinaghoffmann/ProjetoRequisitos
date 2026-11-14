package com.g4.game_tracker_backend.aplicacao.marketplace.negociacao;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnunciosDaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChatRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.PropostaNegociacao;

/**
 * F-19 RN 10, rotina agendada: a proposta aceita sem pedido há mais de 48 horas expira e o anúncio volta a
 * ATIVO. Recebe o instante como parâmetro para os testes não dependerem do relógio.
 */
@Service
public class ExpirarReservasVencidas {

    private final ConversaChatRepositorio conversas;
    private final AnunciosDaNegociacao anuncios;

    public ExpirarReservasVencidas(ConversaChatRepositorio conversas, AnunciosDaNegociacao anuncios) {
        this.conversas = conversas;
        this.anuncios = anuncios;
    }

    /** Devolve quantas reservas expiraram. */
    @Transactional
    public int executar(Instant agora) {
        int expiradas = 0;
        for (ConversaChat conversa : conversas.listarComReservaAceitaAte(agora.minus(PropostaNegociacao.PRAZO_PARA_PAGAR))) {
            ConversaChat atualizada = conversa.expirarReservaVencida(agora);
            if (atualizada != conversa) {
                conversas.salvar(atualizada);
                anuncios.liberar(conversa.anuncioId());
                expiradas++;
            }
        }
        return expiradas;
    }
}
