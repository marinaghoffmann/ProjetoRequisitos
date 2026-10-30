package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.forum.ConsultarForum;
import com.g4.game_tracker_backend.aplicacao.forum.CriarTopico;
import com.g4.game_tracker_backend.aplicacao.forum.GerenciarRespostas;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import com.g4.game_tracker_backend.dominio.forum.FiltroTopicos;
import com.g4.game_tracker_backend.dominio.forum.RespostaForum;
import com.g4.game_tracker_backend.dominio.forum.ResumoTopico;
import com.g4.game_tracker_backend.dominio.forum.StatusTopico;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForumRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/**
 * Steps da F-13 (fórum). Os tópicos de apoio são montados pelo próprio agregado, com datas no
 * passado quando o cenário pede ("publicada há 49 horas"), e gravados direto no repositório.
 * Todos ficam em um jogo exclusivo do cenário, para a listagem não contar tópicos de outros cenários.
 */
public class PassosForum {

    private static final String JOGO_DO_CENARIO = "Jogo do fórum";

    private final CriarTopico criarTopico;
    private final GerenciarRespostas gerenciarRespostas;
    private final ConsultarForum consultarForum;
    private final TopicoDeForumRepositorio topicos;
    private final DadosDoCenario dados;
    private final ContextoDoCenario contexto;

    private final Map<String, UUID> respostasPorAutor = new HashMap<>();
    private TopicoDeForum topicoAtual;
    private UUID minhaResposta;
    private RespostaForum novaResposta;
    private Pagina<ResumoTopico> paginaConsultada;

    public PassosForum(CriarTopico criarTopico, GerenciarRespostas gerenciarRespostas, ConsultarForum consultarForum,
            TopicoDeForumRepositorio topicos, DadosDoCenario dados, ContextoDoCenario contexto) {
        this.criarTopico = criarTopico;
        this.gerenciarRespostas = gerenciarRespostas;
        this.consultarForum = consultarForum;
        this.topicos = topicos;
        this.dados = dados;
        this.contexto = contexto;
    }

    // ---- Criação ----

    @Quando("o jogador cria o tópico {string} no jogo {string}")
    public void jogadorCriaTopico(String titulo, String jogo) {
        topicoAtual = contexto.obter(() -> criarTopico.executar(dados.jogo(jogo).id(), null, titulo,
                "Quero dicas para começar."));
    }

    @Entao("o tópico deve ser criado com status {string} e {int} respostas")
    public void topicoCriado(String status, int respostas) {
        assertNotNull(topicoAtual, () -> "O tópico foi recusado: " + contexto.erro().getMessage());
        TopicoDeForum salvo = recarregar();
        assertEquals(StatusTopico.valueOf(status), salvo.status());
        assertEquals(respostas, salvo.totalRespostas());
    }

    // ---- Tópicos de apoio ----

    @Dado("um tópico aberto com uma resposta de {string}")
    public void topicoComRespostaDe(String autor) {
        topicoAtual = novoTopico();
        responderComo(dados.jogador(autor), Duration.ofHours(1));
        respostasPorAutor.put(autor, topicoAtual.ultimaResposta().id());
        salvar();
    }

    @Dado("um tópico com status {string}")
    public void topicoComStatus(String status) {
        topicoAtual = novoTopico();
        mudarStatus(StatusTopico.valueOf(status));
        salvar();
    }

    @Dado("um tópico com status {string} com uma resposta do jogador")
    public void topicoComStatusERespostaDoJogador(String status) {
        topicoAtual = novoTopico();
        responderComoJogador(Duration.ofHours(1));
        mudarStatus(StatusTopico.valueOf(status));
        salvar();
    }

    @Dado("um tópico aberto com uma resposta do jogador publicada há {int} horas")
    public void topicoComRespostaDoJogadorHa(int horas) {
        topicoAtual = novoTopico();
        responderComoJogador(Duration.ofHours(horas));
        salvar();
    }

    @Dado("um tópico aberto com {int} respostas")
    public void topicoComRespostas(int quantidade) {
        topicoAtual = novoTopico();
        for (int i = 0; i < quantidade; i++) {
            responderComo(dados.jogador("participante " + i), Duration.ofMinutes(quantidade - i + 1L));
        }
        salvar();
    }

    @Dado("um tópico aberto com {int} respostas, sendo uma do jogador")
    public void topicoComRespostasSendoUmaDoJogador(int quantidade) {
        topicoAtual = novoTopico();
        for (int i = 1; i < quantidade; i++) {
            responderComo(dados.jogador("participante " + i), Duration.ofMinutes(quantidade - i + 2L));
        }
        responderComoJogador(Duration.ofMinutes(1));
        salvar();
    }

    @Dado("que o jogo {string} possui {int} tópicos")
    public void jogoPossuiTopicos(String jogo, int quantidade) {
        UUID jogoId = dados.jogo(jogo).id();
        Usuario autor = dados.jogador("autor de apoio");
        Instant base = Instant.now().minus(Duration.ofDays(1));
        for (int i = 0; i < quantidade; i++) {
            TopicoDeForum topico = TopicoDeForum.criar(autor.id(), jogoId, null, "Tópico " + (i + 1),
                    "Conteúdo de apoio", base.plus(Duration.ofMinutes(i)));
            if (i % 3 == 0) {
                topico = topico.responder(dados.jogador("participante").id(), null, "Resposta de apoio",
                        base.plus(Duration.ofMinutes(i)).plusSeconds(30));
            }
            topicos.salvar(topico);
        }
    }

    // ---- Ações ----

    @Quando("o jogador responde à resposta de {string} com {string}")
    public void jogadorRespondeAResposta(String autor, String texto) {
        UUID pai = respostasPorAutor.get(autor);
        novaResposta = contexto.obter(() -> gerenciarRespostas.responder(topicoAtual.id(), pai, texto));
    }

    @Quando("o jogador responde ao tópico com {string}")
    public void jogadorRespondeAoTopico(String texto) {
        novaResposta = contexto.obter(() -> gerenciarRespostas.responder(topicoAtual.id(), null, texto));
    }

    @Quando("o jogador edita a sua resposta")
    public void jogadorEditaSuaResposta() {
        editar(minhaResposta, "Texto editado");
    }

    @Quando("o jogador edita a sua resposta para {string}")
    public void jogadorEditaSuaRespostaPara(String texto) {
        editar(minhaResposta, texto);
    }

    @Quando("o jogador edita a resposta de {string}")
    public void jogadorEditaRespostaDe(String autor) {
        editar(respostasPorAutor.get(autor), "Tentando editar a postagem de outra pessoa");
    }

    @Quando("o jogador exclui a sua resposta")
    public void jogadorExcluiSuaResposta() {
        contexto.executar(() -> gerenciarRespostas.excluir(topicoAtual.id(), minhaResposta));
    }

    @Quando("o usuário consulta a página {int} de tópicos de {string} com {int} itens por página")
    public void usuarioConsultaPagina(int pagina, String jogo, int tamanho) {
        paginaConsultada = contexto.obter(() -> consultarForum.listar(
                new FiltroTopicos(dados.jogo(jogo).id(), null), pagina, tamanho));
    }

    // ---- Verificações ----

    @Entao("a nova resposta deve estar vinculada à resposta de {string}")
    public void novaRespostaVinculada(String autor) {
        assertNotNull(novaResposta, () -> "A resposta foi recusada: " + contexto.erro().getMessage());
        RespostaForum salva = recarregar().resposta(novaResposta.id());
        assertEquals(respostasPorAutor.get(autor), salva.respostaPaiId());
    }

    @Entao("a resposta deve ter o conteúdo {string}")
    public void respostaDeveTerConteudo(String texto) {
        assertEquals(null, contexto.erro(), () -> "A edição foi recusada: " + contexto.erro().getMessage());
        assertEquals(texto, recarregar().resposta(minhaResposta).conteudo());
    }

    /** Confere o agregado e também o contador gravado, que é o usado pela listagem paginada. */
    @Entao("o tópico deve ter {int} respostas")
    public void topicoDeveTerRespostas(int quantidade) {
        assertEquals(null, contexto.erro(), () -> "A ação foi recusada: " + contexto.erro().getMessage());
        assertEquals(quantidade, recarregar().totalRespostas());
        ResumoTopico resumo = consultarForum.listar(new FiltroTopicos(topicoAtual.jogoId(), null), 1, 10)
                .itens().stream().filter(item -> item.id().equals(topicoAtual.id())).findFirst().orElseThrow();
        assertEquals(quantidade, resumo.totalRespostas());
    }

    @Entao("a data da última atividade deve ser a da nova resposta")
    public void ultimaAtividadeDaNovaResposta() {
        TopicoDeForum salvo = recarregar();
        assertEquals(salvo.resposta(novaResposta.id()).publicadaEm(), salvo.dataUltimaAtividade());
    }

    @Entao("a consulta deve retornar {int} tópicos com número de respostas e última interação")
    public void consultaDeveRetornarTopicos(int quantidade) {
        assertNotNull(paginaConsultada, () -> "A consulta foi recusada: " + contexto.erro().getMessage());
        assertEquals(quantidade, paginaConsultada.itens().size());
        assertTrue(paginaConsultada.totalItens() > quantidade, "A consulta deveria ter mais de uma página");
        assertTrue(paginaConsultada.itens().stream().anyMatch(item -> item.totalRespostas() > 0),
                "Algum tópico da página deveria trazer o número de respostas");
        for (ResumoTopico item : paginaConsultada.itens()) {
            assertNotNull(item.dataUltimaAtividade(), "Todo tópico deve trazer a última interação");
        }
        for (int i = 1; i < paginaConsultada.itens().size(); i++) {
            assertTrue(!paginaConsultada.itens().get(i).dataUltimaAtividade()
                    .isAfter(paginaConsultada.itens().get(i - 1).dataUltimaAtividade()),
                    "Os tópicos devem vir do mais recentemente ativo para o menos");
        }
    }

    // ---- Apoio ----

    /** Tópico de outro autor, publicado há 3 dias, para as respostas de apoio caberem depois dele. */
    private TopicoDeForum novoTopico() {
        Jogo jogo = dados.jogo(JOGO_DO_CENARIO);
        return TopicoDeForum.criar(dados.jogador("autor de apoio").id(), jogo.id(), null, "Tópico de apoio",
                "Vamos conversar sobre o jogo.", Instant.now().minus(Duration.ofDays(3)));
    }

    private void responderComo(Usuario autor, Duration haQuantoTempo) {
        topicoAtual = topicoAtual.responder(autor.id(), null, "Resposta de " + autor.nome(),
                Instant.now().minus(haQuantoTempo));
    }

    private void responderComoJogador(Duration haQuantoTempo) {
        responderComo(dados.jogadorAtual(), haQuantoTempo);
        minhaResposta = topicoAtual.ultimaResposta().id();
    }

    private void mudarStatus(StatusTopico status) {
        if (status != StatusTopico.ABERTO) {
            topicoAtual = topicoAtual.alterarStatus(topicoAtual.autorId(), status, Instant.now());
        }
    }

    private void salvar() {
        topicos.salvar(topicoAtual);
    }

    private void editar(UUID respostaId, String texto) {
        contexto.obter(() -> gerenciarRespostas.editar(topicoAtual.id(), respostaId, texto));
    }

    private TopicoDeForum recarregar() {
        return topicos.buscarPorId(topicoAtual.id()).orElseThrow();
    }
}
