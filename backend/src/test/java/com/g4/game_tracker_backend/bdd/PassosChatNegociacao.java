package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.ConsultarConversas;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.ConsultarConversas.ResumoConversa;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.EnviarMensagem;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.ExpirarReservasVencidas;
import com.g4.game_tracker_backend.aplicacao.marketplace.negociacao.GerenciarPropostas;
import com.g4.game_tracker_backend.dominio.marketplace.anuncios.StatusAnuncio;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChat;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.ConversaChatRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.PropostaNegociacao;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.StatusProposta;
import com.g4.game_tracker_backend.dominio.marketplace.negociacao.TipoProposta;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/**
 * Steps da F-19. Anúncio, conversa e proposta do cenário ficam em CenarioDoMarketplace; as ações passam
 * pelos casos de uso reais, autenticando antes quem age.
 */
public class PassosChatNegociacao {

    private final CenarioDoMarketplace cenario;
    private final GerenciarPropostas gerenciarPropostas;
    private final EnviarMensagem enviarMensagem;
    private final ConsultarConversas consultarConversas;
    private final ExpirarReservasVencidas expirarReservas;
    private final ConversaChatRepositorio conversas;
    private final DadosDoCenario dados;
    private final ContextoDoCenario contexto;

    private UUID conversaQueJaExistia;
    private UUID propostaAnterior;
    private UUID conversaDeOutroComprador;
    private List<ResumoConversa> conversasConsultadas;

    public PassosChatNegociacao(CenarioDoMarketplace cenario, GerenciarPropostas gerenciarPropostas,
            EnviarMensagem enviarMensagem, ConsultarConversas consultarConversas,
            ExpirarReservasVencidas expirarReservas, ConversaChatRepositorio conversas, DadosDoCenario dados,
            ContextoDoCenario contexto) {
        this.cenario = cenario;
        this.gerenciarPropostas = gerenciarPropostas;
        this.enviarMensagem = enviarMensagem;
        this.consultarConversas = consultarConversas;
        this.expirarReservas = expirarReservas;
        this.conversas = conversas;
        this.dados = dados;
        this.contexto = contexto;
    }

    // ---- RN 1 e RN 2: abertura da conversa ----

    @Dado("um anúncio {string} do vendedor {string}")
    public void anuncioDoVendedor(String status, String vendedor) {
        cenario.anunciar(vendedor, StatusAnuncio.valueOf(status));
    }

    @Dado("que já existe uma conversa entre {string} e {string} para o anúncio")
    public void jaExisteConversa(String comprador, String vendedor) {
        conversaQueJaExistia = cenario.conversaEntre(comprador, vendedor).id();
    }

    @Quando("o comprador inicia uma conversa sobre o anúncio")
    public void compradorIniciaConversa() {
        String comprador = dados.jogadorAtual().nome();
        ConversaChat conversa = contexto.obter(() -> cenario.abrirConversa(comprador));
        if (conversa != null) {
            cenario.usarConversa(conversa.id());
        }
    }

    @Entao("deve existir {int} conversa entre {string} e {string} para o anúncio")
    public void deveExistirConversa(int quantidade, String comprador, String vendedor) {
        List<ConversaChat> doAnuncio = conversas.listarPorAnuncio(cenario.anuncioId()).stream()
                .filter(c -> c.compradorId().equals(dados.jogador(comprador).id())
                        && c.vendedorId().equals(dados.jogador(vendedor).id()))
                .toList();
        assertEquals(quantidade, doAnuncio.size());
    }

    @Entao("a conversa aberta deve ser a que já existia")
    public void conversaAbertaEhAExistente() {
        assertEquals(conversaQueJaExistia, cenario.conversaId());
    }

    // ---- RN 3, 4 e 5: mensagens ----

    @Dado("uma conversa entre {string} e {string} para um anúncio ativo")
    public void conversaParaAnuncioAtivo(String comprador, String vendedor) {
        cenario.conversaEntre(comprador, vendedor);
    }

    @Dado("uma conversa entre {string} e {string} com {int} mensagens")
    public void conversaComMensagens(String comprador, String vendedor, int quantidade) {
        cenario.conversaEntre(comprador, vendedor);
        for (int i = 0; i < quantidade; i++) {
            enviar(i % 2 == 0 ? comprador : vendedor, "Mensagem " + (i + 1));
        }
    }

    @Dado("que o anúncio da conversa foi vendido")
    public void anuncioDaConversaVendido() {
        cenario.colocarAnuncioEm(StatusAnuncio.VENDIDO);
    }

    @Quando("{string} envia a mensagem {string}")
    public void enviaMensagem(String remetente, String texto) {
        enviar(remetente, texto);
    }

    @Quando("{string} envia uma mensagem com {int} caracteres")
    public void enviaMensagemComTamanho(String remetente, int tamanho) {
        enviar(remetente, "a".repeat(tamanho));
    }

    @Quando("o usuário tenta ler as mensagens da conversa")
    public void usuarioTentaLer() {
        contexto.obter(() -> consultarConversas.detalhar(cenario.conversaId()));
    }

    // ---- RN 6 a 8: propostas ----

    @Dado("uma conversa entre {string} e {string} com uma proposta pendente de R$ {reais}")
    public void conversaComPropostaPendente(String comprador, String vendedor, BigDecimal valor) {
        cenario.conversaEntre(comprador, vendedor);
        cenario.proporValor(comprador, valor);
    }

    @Quando("{string} envia uma proposta de R$ {reais}")
    public void enviaProposta(String remetente, BigDecimal valor) {
        propostaAnterior = cenario.propostaId();
        contexto.obter(() -> cenario.proporValor(remetente, valor));
    }

    @Quando("{string} envia uma proposta de troca pelo jogo {string}")
    public void enviaPropostaDeTroca(String remetente, String jogo) {
        contexto.obter(() -> cenario.proporTroca(remetente, jogo));
    }

    @Quando("{string} cancela a proposta")
    public void cancelaProposta(String usuario) {
        dados.autenticar(usuario);
        contexto.obter(() -> gerenciarPropostas.cancelar(cenario.conversaId(), cenario.propostaId()));
    }

    @Dado("que {string} recusou a proposta")
    @Quando("{string} recusa a proposta")
    public void recusaProposta(String usuario) {
        dados.autenticar(usuario);
        contexto.obter(() -> gerenciarPropostas.recusar(cenario.conversaId(), cenario.propostaId()));
    }

    @Quando("{string} aceita a proposta")
    public void aceitaProposta(String usuario) {
        contexto.obter(() -> cenario.aceitar(usuario));
    }

    @Entao("a proposta deve ter status {string}")
    public void propostaDeveTerStatus(String status) {
        assertEquals(StatusProposta.valueOf(status), cenario.proposta().status(), this::erroAnterior);
    }

    @Entao("a proposta anterior deve ter status {string}")
    public void propostaAnteriorDeveTerStatus(String status) {
        assertEquals(StatusProposta.valueOf(status), cenario.conversa().proposta(propostaAnterior).status());
    }

    @Entao("a proposta deve ser do tipo {string} com status {string}")
    public void propostaDoTipoComStatus(String tipo, String status) {
        PropostaNegociacao proposta = cenario.proposta();
        assertEquals(TipoProposta.valueOf(tipo), proposta.tipo());
        assertEquals(StatusProposta.valueOf(status), proposta.status());
    }

    @Entao("o anúncio negociado deve ficar {string}")
    public void anuncioNegociadoDeveFicar(String status) {
        assertEquals(StatusAnuncio.valueOf(status), cenario.anuncio().status(), this::erroAnterior);
    }

    // ---- RN 9: aceite reserva o item ----

    @Dado("que {string} também enviou uma proposta de R$ {reais} para o mesmo anúncio")
    public void outroCompradorTambemPropos(String comprador, BigDecimal valor) {
        UUID conversaPrincipal = cenario.conversaId();
        UUID propostaPrincipal = cenario.propostaId();
        conversaDeOutroComprador = cenario.abrirConversa(comprador).id();
        cenario.proporValor(comprador, valor);
        cenario.usarConversa(conversaPrincipal);
        cenario.usarProposta(propostaPrincipal);
    }

    @Entao("a proposta de {string} deve ter status {string} com o aviso {string}")
    public void propostaDoOutroComAviso(String comprador, String status, String aviso) {
        PropostaNegociacao proposta = conversas.buscarPorId(conversaDeOutroComprador).orElseThrow()
                .ultimaProposta().orElseThrow();
        assertEquals(StatusProposta.valueOf(status), proposta.status());
        assertEquals(aviso, proposta.motivo());
    }

    // ---- RN 10: prazo de 48 horas ----

    @Dado("uma conversa entre {string} e {string} com uma proposta aceita há {int} horas")
    public void conversaComPropostaAceitaHa(String comprador, String vendedor, int horas) {
        conversaComPropostaPendente(comprador, vendedor, new BigDecimal("120.00"));
        cenario.aceitar(vendedor);
        cenario.recuarAceite(Duration.ofHours(horas));
    }

    @Quando("a rotina de expiração de reservas é executada")
    public void rotinaDeExpiracao() {
        expirarReservas.executar(Instant.now());
    }

    @Quando("{string} libera a reserva")
    public void liberaReserva(String vendedor) {
        dados.autenticar(vendedor);
        contexto.obter(() -> gerenciarPropostas.liberarReserva(cenario.conversaId(), cenario.propostaId()));
    }

    // ---- RN 11 e consulta ----

    @Dado("uma conversa entre {string} e {string} com {int} mensagens de {string} não lidas")
    public void conversaComNaoLidas(String comprador, String vendedor, int quantidade, String remetente) {
        cenario.conversaEntre(comprador, vendedor);
        for (int i = 0; i < quantidade; i++) {
            enviar(remetente, "Mensagem " + (i + 1));
        }
    }

    @Quando("{string} abre a conversa")
    public void abreConversa(String usuario) {
        dados.autenticar(usuario);
        enviarMensagem.marcarComoLidas(cenario.conversaId());
    }

    @Entao("{string} deve ter {int} mensagens não lidas")
    public void deveTerNaoLidas(String usuario, int quantidade) {
        dados.autenticar(usuario);
        assertEquals(quantidade, consultarConversas.contarNaoLidas());
    }

    @Dado("que {string} participa de {int} conversas, uma com {int} mensagens não lidas")
    public void participaDeConversas(String vendedor, int total, int naoLidas) {
        cenario.anunciar(vendedor, StatusAnuncio.ATIVO);
        for (int i = 0; i < total; i++) {
            String comprador = "comprador " + (i + 1);
            cenario.abrirConversa(comprador);
            cenario.proporValor(comprador, new BigDecimal("100.00"));
            if (i == 0) {
                for (int j = 0; j < naoLidas; j++) {
                    enviar(comprador, "Mensagem " + (j + 1));
                }
            }
        }
        dados.autenticar(vendedor);
    }

    @Quando("o jogador consulta suas conversas ativas")
    public void consultaConversas() {
        conversasConsultadas = consultarConversas.listar();
    }

    @Entao("a consulta deve retornar {int} conversas com o status da última proposta")
    public void consultaDeveRetornarConversas(int quantidade) {
        assertEquals(quantidade, conversasConsultadas.size());
        conversasConsultadas.forEach(resumo -> assertNotNull(resumo.statusUltimaProposta()));
    }

    @Entao("uma conversa deve indicar {int} mensagens não lidas")
    public void umaConversaIndicaNaoLidas(int quantidade) {
        assertEquals(1, conversasConsultadas.stream().filter(resumo -> resumo.naoLidas() == quantidade).count());
    }

    // ---- Apoio ----

    private void enviar(String remetente, String texto) {
        dados.autenticar(remetente);
        contexto.obter(() -> enviarMensagem.executar(cenario.conversaId(), texto));
    }

    private String erroAnterior() {
        return contexto.erro() == null ? null : "A ação foi recusada: " + contexto.erro().getMessage();
    }
}
