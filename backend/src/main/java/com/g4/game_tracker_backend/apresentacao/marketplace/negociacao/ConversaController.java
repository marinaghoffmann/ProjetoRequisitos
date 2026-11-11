package com.g4.game_tracker_backend.apresentacao.marketplace.negociacao;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.ConsultarConversas;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.EnviarMensagem;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.GerenciarPropostas;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.IniciarConversa;

import jakarta.validation.Valid;

/** F-19: todas as rotas exigem login; as de uma conversa só respondem às partes (RN 3). */
@RestController
@RequestMapping("/api/marketplace")
public class ConversaController {

    private final IniciarConversa iniciarConversa;
    private final EnviarMensagem enviarMensagem;
    private final GerenciarPropostas gerenciarPropostas;
    private final ConsultarConversas consultarConversas;

    public ConversaController(IniciarConversa iniciarConversa, EnviarMensagem enviarMensagem,
            GerenciarPropostas gerenciarPropostas, ConsultarConversas consultarConversas) {
        this.iniciarConversa = iniciarConversa;
        this.enviarMensagem = enviarMensagem;
        this.gerenciarPropostas = gerenciarPropostas;
        this.consultarConversas = consultarConversas;
    }

    /** RN 1 e RN 2: abre a conversa do comprador com o vendedor do anúncio, ou reabre a existente. */
    @PostMapping("/anuncios/{anuncioId}/conversas")
    public ConversaResponse iniciar(@PathVariable UUID anuncioId) {
        UUID conversaId = iniciarConversa.executar(anuncioId).id();
        return ConversaResponse.de(consultarConversas.detalhar(conversaId));
    }

    @GetMapping("/conversas")
    public List<ResumoConversaResponse> listar() {
        return consultarConversas.listar().stream().map(ResumoConversaResponse::de).toList();
    }

    /** RN 11: total de mensagens não lidas, para o menu. */
    @GetMapping("/conversas/nao-lidas")
    public Map<String, Integer> naoLidas() {
        return Map.of("total", consultarConversas.contarNaoLidas());
    }

    @GetMapping("/conversas/{id}")
    public ConversaResponse consultar(@PathVariable UUID id) {
        return ConversaResponse.de(consultarConversas.detalhar(id));
    }

    /** RN 11: chamado ao abrir a conversa. */
    @PostMapping("/conversas/{id}/leitura")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarComoLidas(@PathVariable UUID id) {
        enviarMensagem.marcarComoLidas(id);
    }

    @PostMapping("/conversas/{id}/mensagens")
    @ResponseStatus(HttpStatus.CREATED)
    public MensagemResponse enviarMensagem(@PathVariable UUID id, @RequestBody MensagemRequest request) {
        return MensagemResponse.de(enviarMensagem.executar(id, request.texto()));
    }

    @PostMapping("/conversas/{id}/propostas")
    @ResponseStatus(HttpStatus.CREATED)
    public PropostaResponse enviarProposta(@PathVariable UUID id, @Valid @RequestBody PropostaRequest request) {
        return PropostaResponse.de(gerenciarPropostas.enviar(id, request.tipo(), request.valor(),
                request.jogoOferecido(), request.mensagem()));
    }

    @PostMapping("/conversas/{id}/propostas/{propostaId}/aceite")
    public PropostaResponse aceitar(@PathVariable UUID id, @PathVariable UUID propostaId) {
        return PropostaResponse.de(gerenciarPropostas.aceitar(id, propostaId));
    }

    @PostMapping("/conversas/{id}/propostas/{propostaId}/recusa")
    public PropostaResponse recusar(@PathVariable UUID id, @PathVariable UUID propostaId) {
        return PropostaResponse.de(gerenciarPropostas.recusar(id, propostaId));
    }

    @PostMapping("/conversas/{id}/propostas/{propostaId}/cancelamento")
    public PropostaResponse cancelar(@PathVariable UUID id, @PathVariable UUID propostaId) {
        return PropostaResponse.de(gerenciarPropostas.cancelar(id, propostaId));
    }

    /** RN 10: o vendedor libera a reserva antes do prazo de 48 horas. */
    @PostMapping("/conversas/{id}/propostas/{propostaId}/liberacao")
    public PropostaResponse liberarReserva(@PathVariable UUID id, @PathVariable UUID propostaId) {
        return PropostaResponse.de(gerenciarPropostas.liberarReserva(id, propostaId));
    }
}
