package com.g4.game_tracker_backend.aplicacao.marketplace.negociacao;

import java.time.Instant;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.compartilhado.ExecutorDeTransacao;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnunciosDaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChatRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.MensagemChat;

/** F-19 RN 3, RN 4 e RN 5: uma das partes envia uma mensagem enquanto o anúncio não foi encerrado. */
public class EnviarMensagem {

    private final ConversaChatRepositorio conversas;
    private final AnunciosDaNegociacao anuncios;
    private final VerificadorDeAcesso verificadorDeAcesso;
    private final ExecutorDeTransacao transacao;

    public EnviarMensagem(ConversaChatRepositorio conversas, AnunciosDaNegociacao anuncios,
            VerificadorDeAcesso verificadorDeAcesso,
            ExecutorDeTransacao transacao) {
        this.conversas = conversas;
        this.anuncios = anuncios;
        this.verificadorDeAcesso = verificadorDeAcesso;
        this.transacao = transacao;
    }

    public MensagemChat executar(UUID conversaId, String texto) {
        return transacao.executar(() -> {
            UUID remetenteId = verificadorDeAcesso.usuarioAtualId();
            ConversaChat conversa = AcessoAConversa.doParticipante(conversas, conversaId);
            ConversaChat atualizada = conversas.salvar(conversa.enviarMensagem(remetenteId, texto,
                    AcessoAConversa.anuncio(anuncios, conversa.anuncioId()), Instant.now()));
            return atualizada.ultimaMensagem().orElseThrow();
        });
    }

    /** RN 11: abrir a conversa marca como lidas as mensagens recebidas. */
    public void marcarComoLidas(UUID conversaId) {
        transacao.executarSemRetorno(() -> {
            UUID leitorId = verificadorDeAcesso.usuarioAtualId();
            ConversaChat conversa = AcessoAConversa.doParticipante(conversas, conversaId);
            ConversaChat lida = conversa.marcarComoLidas(leitorId, Instant.now());
            if (lida != conversa) {
                conversas.salvar(lida);
            }
        });
    }
}
