package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.g4.game_tracker_backend.aplicacao.marketplace.reputacao.AvaliarNegociacao;
import com.g4.game_tracker_backend.aplicacao.marketplace.reputacao.ConsultarReputacao;
import com.g4.game_tracker_backend.aplicacao.marketplace.reputacao.ConsultarReputacao.PerfilDeVendedor;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.FormaPagamento;
import com.g4.game_tracker_backend.dominio.marketplace.pedidos.StatusPedido;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.AvaliacaoMarketplace;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.AvaliacaoMarketplaceRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.ReputacaoUsuario;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.ReputacaoUsuarioRepositorio;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.TagComportamento;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/**
 * Steps da F-21. Os pedidos são reais (F-20), criados por PedidosDoCenario e concluídos pela confirmação do
 * recebimento; avaliação e reputação também. Quem avalia é autenticado antes da ação, como numa requisição.
 */
public class PassosReputacao {

    private static final BigDecimal VALOR_DO_PEDIDO = new BigDecimal("100.00");

    private final AvaliarNegociacao avaliarNegociacao;
    private final ConsultarReputacao consultarReputacao;
    private final AvaliacaoMarketplaceRepositorio avaliacoes;
    private final ReputacaoUsuarioRepositorio reputacoes;
    private final PedidosDoCenario pedidos;
    private final DadosDoCenario dados;
    private final ContextoDoCenario contexto;

    private UUID pedidoAtual;
    private PerfilDeVendedor perfilConsultado;

    public PassosReputacao(AvaliarNegociacao avaliarNegociacao, ConsultarReputacao consultarReputacao,
            AvaliacaoMarketplaceRepositorio avaliacoes, ReputacaoUsuarioRepositorio reputacoes,
            PedidosDoCenario pedidos, DadosDoCenario dados, ContextoDoCenario contexto) {
        this.avaliarNegociacao = avaliarNegociacao;
        this.consultarReputacao = consultarReputacao;
        this.avaliacoes = avaliacoes;
        this.reputacoes = reputacoes;
        this.pedidos = pedidos;
        this.dados = dados;
        this.contexto = contexto;
    }

    // ---- Pedidos (F-20) ----

    @Dado("um pedido {string} entre o comprador {string} e o vendedor {string}")
    public void pedidoEntre(String status, String comprador, String vendedor) {
        pedidoAtual = pedidos.criar(comprador, vendedor, VALOR_DO_PEDIDO, StatusPedido.valueOf(status),
                FormaPagamento.PIX);
    }

    /** A entrega é confirmada na conclusão do pedido; recuar as datas simula os dias passados desde então. */
    @Dado("um pedido {string} entregue há {int} dias entre {string} e {string}")
    public void pedidoEntregueHa(String status, int dias, String comprador, String vendedor) {
        pedidoEntre(status, comprador, vendedor);
        pedidos.recuar(Duration.ofDays(dias));
    }

    // ---- Avaliações ----

    @Quando("{string} avalia {string} com {int} estrelas e as tags {string}")
    public void avaliaComTags(String avaliador, String avaliado, int estrelas, String tags) {
        avaliar(avaliador, estrelas, Arrays.stream(tags.split(",")).map(String::trim)
                .map(TagComportamento::valueOf).collect(Collectors.toSet()));
    }

    @Quando("{string} avalia {string} com {int} estrelas")
    public void avalia(String avaliador, String avaliado, int estrelas) {
        avaliar(avaliador, estrelas, Set.of());
    }

    @Dado("que {string} já avaliou {string} neste pedido")
    public void jaAvaliou(String avaliador, String avaliado) {
        assertNotNull(avaliar(avaliador, 4, Set.of()), () -> "A avaliação de apoio foi recusada: "
                + contexto.erro().getMessage());
    }

    // ---- Reputação ----

    @Dado("^que \"([^\"]*)\" tem reputação (\\d+\\.\\d{2}) com (\\d+) avaliações e (\\d+) negociações bem-sucedidas$")
    public void temReputacao(String nome, String media, int total, int negociacoes) {
        int somaEstrelas = new BigDecimal(media).multiply(BigDecimal.valueOf(total)).intValueExact();
        reputacoes.salvar(ReputacaoUsuario.reconstituir(dados.jogador(nome).id(), somaEstrelas, total, negociacoes,
                Instant.now()));
    }

    @Dado("^que \"([^\"]*)\" recebeu avaliações de ([\\d, e]+) estrelas$")
    public void recebeuAvaliacoes(String vendedor, String notas) {
        String[] estrelas = notas.split(",| e ");
        for (int i = 0; i < estrelas.length; i++) {
            String comprador = "comprador " + (i + 1);
            pedidoEntre("CONCLUIDO", comprador, vendedor);
            avaliar(comprador, Integer.parseInt(estrelas[i].trim()), Set.of());
        }
    }

    @Quando("um usuário consulta o perfil de vendedor de {string}")
    public void consultaPerfil(String vendedor) {
        perfilConsultado = consultarReputacao.perfil(dados.jogador(vendedor).id());
    }

    // ---- Verificações ----

    @Entao("^\"([^\"]*)\" deve ter (\\d+) (?:avaliação recebida|avaliações recebidas)$")
    public void deveTerAvaliacoesRecebidas(String nome, int quantidade) {
        assertEquals(quantidade, consultarReputacao.perfil(dados.jogador(nome).id()).avaliacoesRecebidas().size());
    }

    @Entao("o pedido deve ter {int} avaliações")
    public void pedidoDeveTerAvaliacoes(int quantidade) {
        assertEquals(quantidade, avaliacoes.contarPorPedido(pedidoAtual));
    }

    @Entao("^a reputação de \"([^\"]*)\" deve ser (\\d+\\.\\d{2})$")
    public void reputacaoDeveSer(String nome, String media) {
        assertEquals(new BigDecimal(media), reputacao(nome).notaMedia());
    }

    @Entao("{string} deve ter {int} negociações bem-sucedidas")
    public void deveTerNegociacoes(String nome, int quantidade) {
        assertEquals(quantidade, reputacao(nome).negociacoesBemSucedidas());
    }

    @Entao("^a consulta deve retornar (\\d+) avaliações e a nota média (\\d+\\.\\d{2})$")
    public void consultaDeveRetornar(int quantidade, String media) {
        assertEquals(quantidade, perfilConsultado.avaliacoesRecebidas().size());
        assertEquals(new BigDecimal(media), perfilConsultado.reputacao().notaMedia());
    }

    // ---- Apoio ----

    private AvaliacaoMarketplace avaliar(String avaliador, int estrelas, Set<TagComportamento> tags) {
        dados.autenticar(avaliador);
        return contexto.obter(() -> avaliarNegociacao.executar(pedidoAtual, estrelas, tags, null));
    }

    private ReputacaoUsuario reputacao(String nome) {
        return reputacoes.buscarPorUsuario(dados.jogador(nome).id()).orElseThrow();
    }
}
