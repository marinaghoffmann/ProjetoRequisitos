package com.g4.game_tracker_backend.aplicacao.marketplace.negociacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnuncioNegociavel;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.AnunciosDaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChatRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.PropostaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.TipoProposta;

/**
 * F-19 RN 6 a 10: envio, resposta e cancelamento das propostas formais, e a reserva que o aceite cria.
 * O aceite, a recusa automática das outras conversas e a reserva do anúncio acontecem na mesma transação.
 */
@Service
public class GerenciarPropostas {

    private final ConversaChatRepositorio conversas;
    private final AnunciosDaNegociacao anuncios;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public GerenciarPropostas(ConversaChatRepositorio conversas, AnunciosDaNegociacao anuncios,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.conversas = conversas;
        this.anuncios = anuncios;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    /** RN 6 e 7. A mensagem é opcional e entra no chat junto com a proposta. */
    @Transactional
    public PropostaNegociacao enviar(UUID conversaId, TipoProposta tipo, BigDecimal valor, String jogoOferecido,
            String mensagem) {
        UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
        ConversaChat conversa = AcessoAConversa.doParticipante(conversas, conversaId, usuarioId);
        AnuncioNegociavel anuncio = AcessoAConversa.anuncio(anuncios, conversa.anuncioId());
        ConversaChat atualizada = conversas.salvar(conversa.enviarProposta(usuarioId, tipo, valor, jogoOferecido,
                mensagem, anuncio, Instant.now()));
        return atualizada.ultimaProposta().orElseThrow();
    }

    /** RN 9: aceita, reserva o anúncio e recusa as pendentes das outras conversas do mesmo anúncio. */
    @Transactional
    public PropostaNegociacao aceitar(UUID conversaId, UUID propostaId) {
        UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
        Instant agora = Instant.now();
        ConversaChat conversa = AcessoAConversa.doParticipante(conversas, conversaId, usuarioId);
        AnuncioNegociavel anuncio = AcessoAConversa.anuncio(anuncios, conversa.anuncioId());
        ConversaChat aceita = conversas.salvar(conversa.aceitarProposta(usuarioId, propostaId, anuncio, agora));
        anuncios.reservar(anuncio.anuncioId());
        recusarOutrasConversas(anuncio.anuncioId(), conversaId, agora);
        return aceita.proposta(propostaId);
    }

    @Transactional
    public PropostaNegociacao recusar(UUID conversaId, UUID propostaId) {
        UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
        ConversaChat conversa = AcessoAConversa.doParticipante(conversas, conversaId, usuarioId);
        return conversas.salvar(conversa.recusarProposta(usuarioId, propostaId, Instant.now())).proposta(propostaId);
    }

    @Transactional
    public PropostaNegociacao cancelar(UUID conversaId, UUID propostaId) {
        UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
        ConversaChat conversa = AcessoAConversa.doParticipante(conversas, conversaId, usuarioId);
        return conversas.salvar(conversa.cancelarProposta(usuarioId, propostaId, Instant.now())).proposta(propostaId);
    }

    /** RN 10: o vendedor libera a reserva antes do prazo e o anúncio volta a ATIVO. */
    @Transactional
    public PropostaNegociacao liberarReserva(UUID conversaId, UUID propostaId) {
        UUID usuarioId = verificadorDeAcesso.usuarioAtualId();
        ConversaChat conversa = AcessoAConversa.doParticipante(conversas, conversaId, usuarioId);
        ConversaChat liberada = conversas.salvar(conversa.liberarReserva(usuarioId, propostaId, Instant.now()));
        anuncios.liberar(conversa.anuncioId());
        return liberada.proposta(propostaId);
    }

    /** RN 9, também usada pela compra direta da F-20: o item foi reservado para outro comprador. */
    void recusarOutrasConversas(UUID anuncioId, UUID conversaQueReservou, Instant agora) {
        for (ConversaChat outra : conversas.listarPorAnuncio(anuncioId)) {
            if (outra.id().equals(conversaQueReservou)) {
                continue;
            }
            ConversaChat recusada = outra.recusarPorReservaDeOutroComprador(agora);
            if (recusada != outra) {
                conversas.salvar(recusada);
            }
        }
    }
}
