package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.biblioteca.listas.ConsultarListas;
import com.g4.game_tracker_backend.aplicacao.biblioteca.listas.GerenciarItensDaLista;
import com.g4.game_tracker_backend.aplicacao.biblioteca.listas.GerenciarListas;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaPersonalizada;
import com.g4.game_tracker_backend.dominio.biblioteca.listas.ListaRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/**
 * Steps da F-04 (listas personalizadas). As ações passam pelos casos de uso, autenticadas como "o
 * jogador" do cenário (DadosDoCenario). Os jogos de apoio também vêm de DadosDoCenario, um por cenário.
 * Listas com muitos jogos são montadas pelo agregado e gravadas de uma vez, para o cenário não demorar.
 */
public class PassosListasPersonalizadas {

    private final GerenciarListas gerenciarListas;
    private final GerenciarItensDaLista gerenciarItens;
    private final ConsultarListas consultarListas;
    private final ListaRepositorio listas;
    private final DadosDoCenario dados;
    private final ContextoDoCenario contexto;

    private final Map<String, UUID> listasDoJogador = new HashMap<>();
    private UUID listaAtual;
    private List<ListaPersonalizada> consulta;

    public PassosListasPersonalizadas(GerenciarListas gerenciarListas, GerenciarItensDaLista gerenciarItens,
            ConsultarListas consultarListas, ListaRepositorio listas, DadosDoCenario dados,
            ContextoDoCenario contexto) {
        this.gerenciarListas = gerenciarListas;
        this.gerenciarItens = gerenciarItens;
        this.consultarListas = consultarListas;
        this.listas = listas;
        this.dados = dados;
        this.contexto = contexto;
    }

    // ---- Listas ----

    @Quando("o jogador cria a lista {string} com descrição {string}")
    public void jogadorCriaListaComDescricao(String titulo, String descricao) {
        criar(titulo, descricao);
    }

    @Quando("o jogador cria a lista {string}")
    public void jogadorCriaLista(String titulo) {
        criar(titulo, null);
    }

    @Dado("que o jogador já possui a lista {string}")
    public void jogadorJaPossuiLista(String titulo) {
        criar(titulo, null);
        assertNull(contexto.erro(), () -> "A lista de apoio foi recusada: " + contexto.erro().getMessage());
    }

    @Dado("que o jogador {string} possui a lista {string}")
    public void outroJogadorPossuiLista(String nome, String titulo) {
        Usuario outro = dados.jogador(nome);
        SessaoDeTeste.autenticarComo(outro);
        try {
            gerenciarListas.criar(titulo, null);
        } finally {
            SessaoDeTeste.autenticarComo(dados.jogadorAtual());
        }
    }

    @Quando("o jogador renomeia a lista {string} para {string}")
    public void jogadorRenomeiaLista(String titulo, String novoTitulo) {
        UUID id = lista(titulo);
        ListaPersonalizada renomeada = contexto.obter(() -> gerenciarListas.editar(id, novoTitulo, null));
        assertNotNull(renomeada, () -> "A renomeação foi recusada: " + contexto.erro().getMessage());
        listasDoJogador.remove(titulo);
        listasDoJogador.put(renomeada.titulo(), id);
    }

    @Quando("o jogador exclui a lista {string}")
    public void jogadorExcluiLista(String titulo) {
        UUID id = lista(titulo);
        contexto.executar(() -> gerenciarListas.excluir(id));
    }

    @Entao("o jogador deve possuir a lista {string}")
    public void jogadorDevePossuirLista(String titulo) {
        assertNull(contexto.erro(), () -> "A criação foi recusada: " + contexto.erro().getMessage());
        assertTrue(consultarListas.minhas().stream().anyMatch(lista -> lista.titulo().equals(titulo)),
                "O jogador não possui a lista " + titulo);
    }

    @Entao("o jogador não deve possuir nenhuma lista")
    public void jogadorNaoDevePossuirLista() {
        assertNull(contexto.erro(), () -> "A ação foi recusada: " + contexto.erro().getMessage());
        assertTrue(consultarListas.minhas().isEmpty());
    }

    // ---- Itens ----

    @Quando("o jogador adiciona o jogo {string} à lista com a nota {string}")
    public void jogadorAdicionaJogoComNota(String jogo, String nota) {
        contexto.obter(() -> gerenciarItens.adicionar(listaAtual, dados.jogo(jogo).id(), nota));
    }

    @Quando("o jogador adiciona o jogo {string} à lista")
    public void jogadorAdicionaJogo(String jogo) {
        contexto.obter(() -> gerenciarItens.adicionar(listaAtual, dados.jogo(jogo).id(), null));
    }

    @Quando("o jogador remove o jogo {string} da lista")
    public void jogadorRemoveJogo(String jogo) {
        contexto.obter(() -> gerenciarItens.remover(listaAtual, dados.jogo(jogo).id()));
    }

    @Dado("que a lista {string} do jogador já contém o jogo {string}")
    public void listaJaContemJogo(String titulo, String jogo) {
        jogadorJaPossuiLista(titulo);
        gerenciarItens.adicionar(lista(titulo), dados.jogo(jogo).id(), null);
    }

    @Dado("que a lista {string} do jogador já contém {int} jogos")
    public void listaJaContemJogos(String titulo, int quantidade) {
        jogadorJaPossuiLista(titulo);
        preencher(titulo, quantidade);
    }

    @Entao("a lista {string} deve conter {int} jogo(s)")
    public void listaDeveConter(String titulo, int quantidade) {
        assertNull(contexto.erro(), () -> "A ação foi recusada: " + contexto.erro().getMessage());
        assertEquals(quantidade, consultarListas.buscar(lista(titulo)).totalJogos());
    }

    @Entao("o item {string} deve ter a nota explicativa {string}")
    public void itemDeveTerNota(String jogo, String nota) {
        ListaPersonalizada lista = consultarListas.buscar(listaAtual);
        assertEquals(nota, lista.item(dados.jogo(jogo).id()).orElseThrow().notaExplicativa());
    }

    // ---- Consulta ----

    @Dado("que o jogador possui a lista {string} com {int} jogos e a lista {string} com {int} jogos")
    public void jogadorPossuiListasComJogos(String primeira, int jogosDaPrimeira, String segunda,
            int jogosDaSegunda) {
        jogadorJaPossuiLista(primeira);
        preencher(primeira, jogosDaPrimeira);
        jogadorJaPossuiLista(segunda);
        preencher(segunda, jogosDaSegunda);
    }

    @Quando("o jogador consulta suas listas")
    public void jogadorConsultaListas() {
        consulta = consultarListas.minhas();
    }

    @Entao("a consulta deve retornar {int} listas")
    public void consultaDeveRetornarListas(int quantidade) {
        assertNotNull(consulta, "As listas não foram consultadas");
        assertEquals(quantidade, consulta.size());
    }

    @Entao("a lista {string} deve informar o total de {int} jogos")
    public void listaDeveInformarTotal(String titulo, int total) {
        ListaPersonalizada lista = consulta.stream()
                .filter(consultada -> consultada.titulo().equals(titulo))
                .findFirst()
                .orElseThrow();
        assertEquals(total, lista.totalJogos());
        assertEquals(total, lista.itens().size());
    }

    // ---- Apoio ----

    private void criar(String titulo, String descricao) {
        ListaPersonalizada criada = contexto.obter(() -> gerenciarListas.criar(titulo, descricao));
        if (criada != null) {
            listasDoJogador.put(criada.titulo(), criada.id());
            listaAtual = criada.id();
        }
    }

    private UUID lista(String titulo) {
        listaAtual = listasDoJogador.get(titulo);
        assertNotNull(listaAtual, "O jogador não criou a lista " + titulo + " neste cenário");
        return listaAtual;
    }

    /** Monta a lista com jogos de apoio pelo próprio agregado e grava de uma vez. */
    private void preencher(String titulo, int quantidade) {
        UUID dono = dados.jogadorAtual().id();
        ListaPersonalizada lista = listas.buscarPorId(lista(titulo)).orElseThrow();
        for (int i = 1; i <= quantidade; i++) {
            lista = lista.adicionarJogo(dono, dados.jogo(titulo + " " + i).id(), null, Instant.now());
        }
        listas.salvar(lista);
    }
}
