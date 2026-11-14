package com.g4.game_tracker_backend.bdd;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.GerenciarPropostas;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.IniciarConversa;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.AnuncioMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.EstadoConservacao;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.MidiaJogo;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.TipoNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChatRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.PropostaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.TipoProposta;

import io.cucumber.spring.ScenarioScope;

/**
 * Anúncio, conversa e proposta de UM cenário do marketplace, compartilhados pelos steps da F-19 e da F-20.
 * Cada ação autentica antes quem a executa, como numa requisição, e usa os casos de uso reais.
 */
@Component
@ScenarioScope
public class CenarioDoMarketplace {

    static final BigDecimal PRECO_PADRAO = new BigDecimal("150.00");

    private final AnuncioMarketplaceRepositorio anuncios;
    private final ConversaChatRepositorio conversas;
    private final IniciarConversa iniciarConversa;
    private final GerenciarPropostas gerenciarPropostas;
    private final DadosDoCenario dados;

    private UUID anuncioId;
    private UUID conversaId;
    private UUID propostaId;

    public CenarioDoMarketplace(AnuncioMarketplaceRepositorio anuncios, ConversaChatRepositorio conversas,
            IniciarConversa iniciarConversa, GerenciarPropostas gerenciarPropostas, DadosDoCenario dados) {
        this.anuncios = anuncios;
        this.conversas = conversas;
        this.iniciarConversa = iniciarConversa;
        this.gerenciarPropostas = gerenciarPropostas;
        this.dados = dados;
    }

    // ---- Anúncio ----

    /** Cria o anúncio direto no repositório e o leva ao status pedido pelas transições do contrato. */
    AnuncioMarketplace anunciar(String vendedor, StatusAnuncio status, TipoNegociacao tipo, BigDecimal preco) {
        Instant agora = Instant.now();
        AnuncioMarketplace anuncio = AnuncioMarketplace.criar(dados.jogador(vendedor).id(),
                dados.jogo("Hollow Knight").id(), "Hollow Knight", EstadoConservacao.USADO,
                preco, tipo, MidiaJogo.FISICO, "Recife - PE", List.of("/uploads/marketplace/foto-bdd.png"), agora);
        for (StatusAnuncio passo : caminhoAte(status)) {
            anuncio = anuncio.mudarStatus(passo, agora).anuncio();
        }
        anuncioId = anuncios.salvar(anuncio).id();
        return anuncio;
    }

    AnuncioMarketplace anunciar(String vendedor, StatusAnuncio status) {
        return anunciar(vendedor, status, TipoNegociacao.AMBOS, PRECO_PADRAO);
    }

    AnuncioMarketplace anuncio() {
        return anuncios.buscarPorId(anuncioId).orElseThrow();
    }

    UUID anuncioId() {
        return anuncioId;
    }

    /** Leva o anúncio ao status pelo ciclo de vida, sem passar pelos casos de uso (só para preparar cenários). */
    void colocarAnuncioEm(StatusAnuncio status) {
        AnuncioMarketplace anuncio = anuncio();
        Instant agora = Instant.now();
        if (status == StatusAnuncio.VENDIDO && anuncio.status() == StatusAnuncio.ATIVO) {
            anuncio = anuncio.mudarStatus(StatusAnuncio.EM_NEGOCIACAO, agora).anuncio();
        }
        anuncios.salvar(anuncio.mudarStatus(status, agora).anuncio());
    }

    private static List<StatusAnuncio> caminhoAte(StatusAnuncio status) {
        return switch (status) {
            case ATIVO -> List.of();
            case PAUSADO -> List.of(StatusAnuncio.PAUSADO);
            case CANCELADO -> List.of(StatusAnuncio.CANCELADO);
            case EM_NEGOCIACAO -> List.of(StatusAnuncio.EM_NEGOCIACAO);
            case VENDIDO -> List.of(StatusAnuncio.EM_NEGOCIACAO, StatusAnuncio.VENDIDO);
        };
    }

    // ---- Conversa ----

    ConversaChat abrirConversa(String comprador) {
        dados.autenticar(comprador);
        ConversaChat conversa = iniciarConversa.executar(anuncioId);
        conversaId = conversa.id();
        return conversa;
    }

    /** Anúncio ativo de vendedor com a conversa aberta pelo comprador. */
    ConversaChat conversaEntre(String comprador, String vendedor) {
        anunciar(vendedor, StatusAnuncio.ATIVO);
        return abrirConversa(comprador);
    }

    ConversaChat conversa() {
        return conversas.buscarPorId(conversaId).orElseThrow();
    }

    UUID conversaId() {
        return conversaId;
    }

    void usarConversa(UUID id) {
        conversaId = id;
    }

    // ---- Propostas ----

    PropostaNegociacao proporValor(String comprador, BigDecimal valor) {
        dados.autenticar(comprador);
        PropostaNegociacao proposta = gerenciarPropostas.enviar(conversaId, TipoProposta.VALOR, valor, null, null);
        propostaId = proposta.id();
        return proposta;
    }

    PropostaNegociacao proporTroca(String comprador, String jogo) {
        dados.autenticar(comprador);
        PropostaNegociacao proposta = gerenciarPropostas.enviar(conversaId, TipoProposta.TROCA, null, jogo, null);
        propostaId = proposta.id();
        return proposta;
    }

    PropostaNegociacao aceitar(String vendedor) {
        dados.autenticar(vendedor);
        return gerenciarPropostas.aceitar(conversaId, propostaId);
    }

    UUID propostaId() {
        return propostaId;
    }

    void usarProposta(UUID id) {
        propostaId = id;
    }

    PropostaNegociacao proposta() {
        return conversa().proposta(propostaId);
    }

    /** Simula a passagem do tempo desde o aceite (RN 10), recuando a data de resposta gravada. */
    void recuarAceite(Duration quanto) {
        ConversaChat atual = conversa();
        List<PropostaNegociacao> propostas = atual.propostas().stream()
                .map(p -> p.id().equals(propostaId)
                        ? PropostaNegociacao.reconstituir(p.id(), p.tipo(), p.valorOferecido(), p.jogoOferecido(),
                                p.status(), p.motivo(), p.pedidoId(), p.enviadaEm().minus(quanto),
                                p.respondidaEm().minus(quanto))
                        : p)
                .toList();
        conversas.salvar(ConversaChat.reconstituir(atual.id(), atual.anuncioId(), atual.compradorId(),
                atual.vendedorId(), atual.iniciadaEm(), atual.ultimaAtividadeEm(), atual.mensagens(), propostas));
    }
}
