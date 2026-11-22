package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import com.g4.game_tracker_backend.aplicacao.biblioteca.acervo.GarantirNaBiblioteca;
import com.g4.game_tracker_backend.aplicacao.journal.ConsultarJournal;
import com.g4.game_tracker_backend.aplicacao.journal.ConsultarTimeline;
import com.g4.game_tracker_backend.aplicacao.journal.GerenciarJournal;
import com.g4.game_tracker_backend.aplicacao.journal.GerenciarJournal.DadosEntrada;
import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournal;
import com.g4.game_tracker_backend.dominio.journal.FiltroJournal;
import com.g4.game_tracker_backend.dominio.journal.ImagemDoJournal;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

public class PassosJournal {
    private final DadosDoCenario dados;
    private final ContextoDoCenario contexto;
    private final GerenciarJournal gerenciar;
    private final ConsultarJournal consultar;
    private final ConsultarTimeline timeline;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transacao;
    private final GarantirNaBiblioteca garantir;
    private UUID entradaId;
    private UUID donoId;
    private UUID descartada;
    private String textoOriginal;
    private ConsultarJournal.Entrada publica;
    private Pagina<ConsultarJournal.Entrada> resultado;
    private long imagensAntes;

    public PassosJournal(DadosDoCenario dados, ContextoDoCenario contexto, GerenciarJournal gerenciar,
            ConsultarJournal consultar, ConsultarTimeline timeline, JdbcTemplate jdbc, TransactionTemplate transacao,
            GarantirNaBiblioteca garantir) {
        this.dados = dados; this.contexto = contexto; this.gerenciar = gerenciar;
        this.consultar = consultar; this.timeline = timeline; this.jdbc = jdbc; this.transacao = transacao;
        this.garantir = garantir;
    }

    /** Coloca o jogo na biblioteca do jogador atual (regra: só registra no Journal jogo da biblioteca). */
    private UUID naBiblioteca(String jogo) {
        var id = dados.jogo(jogo).id();
        garantir.executar(dados.jogadorAtual().id(), id);
        return id;
    }

    @Quando("o jogador cria uma entrada no journal sobre {string} com as tags {string}")
    public void criarComTags(String jogo, String tags) {
        contexto.executar(() -> guardar(gerenciar.criar(new DadosEntrada(naBiblioteca(jogo), LocalDate.now(),
                "Uma descoberta", "Explorei o mundo", List.of(tags.split(",")), false), List.of())));
    }

    @Quando("o jogador cria uma entrada no journal sobre {string} sem ter o jogo na biblioteca")
    public void criarSemBiblioteca(String jogo) {
        contexto.executar(() -> guardar(gerenciar.criar(new DadosEntrada(dados.jogo(jogo).id(), LocalDate.now(),
                "Uma descoberta", "Explorei o mundo", List.of(), false), List.of())));
    }

    @Quando("o jogador troca o jogo da entrada por {string} sem tê-lo na biblioteca")
    public void trocarJogoSemBiblioteca(String jogo) {
        contexto.executar(() -> gerenciar.editar(entradaId, new DadosEntrada(dados.jogo(jogo).id(), LocalDate.now(),
                "Sessão de jogo", textoOriginal, List.of(), false), null, List.of()));
    }

    @Dado("que o jogador possui uma entrada no journal com o texto {string}")
    public void prepararTexto(String texto) { guardar(criar(texto, LocalDate.now(), false, 0)); }

    @Dado("que o jogador {string} possui uma entrada no journal")
    public void outroDono(String nome) { dados.autenticar(nome); prepararTexto("Diário de " + nome); }

    @Quando("o jogador {string} tenta excluir a entrada de {string}")
    public void excluirOutro(String solicitante, String dono) {
        dados.autenticar(solicitante);
        contexto.executar(() -> gerenciar.excluir(entradaId));
    }

    @Quando("o jogador {string} tenta editar a entrada de {string}")
    public void editarOutro(String solicitante, String dono) {
        dados.autenticar(solicitante);
        contexto.executar(() -> gerenciar.editar(entradaId, novosDados("Não deve ser salvo"), null, List.of()));
    }

    @Quando("o jogador altera o texto da entrada para {string}")
    public void editarTexto(String texto) {
        contexto.executar(() -> gerenciar.editar(entradaId, novosDados(texto), null, List.of()));
    }

    @Dado("que o jogador possui uma entrada no journal com {int} imagens")
    public void prepararImagens(int quantidade) { guardar(criar("Sessão com imagens", LocalDate.now(), false, quantidade)); }

    @Quando("o jogador anexa mais {int} imagem à entrada")
    public void anexar(int quantidade) {
        contexto.executar(() -> { for (int i = 0; i < quantidade; i++) gerenciar.anexarImagem(entradaId, imagem()); });
    }

    @Quando("o jogador exclui a entrada")
    public void excluir() { contexto.executar(() -> gerenciar.excluir(entradaId)); }

    @Dado("que o jogador possui uma entrada pública marcada com spoiler")
    public void prepararSpoiler() { guardar(criar("O segredo do final", LocalDate.now(), true, 0)); }

    @Dado("o jogador anexa uma imagem ao spoiler")
    public void imagemNoSpoiler() { gerenciar.anexarImagem(entradaId, imagem()); }

    @Quando("outro usuário visualiza o perfil público do jogador")
    public void verPublico() {
        dados.autenticar("visitante");
        publica = consultar.publicas(donoId, FiltroJournal.todos(), 1, 20).itens().getFirst();
    }

    @Quando("outro usuário confirma que deseja ver o spoiler")
    public void revelar() {
        dados.autenticar("visitante"); publica = consultar.publica(donoId, entradaId, true);
    }

    @Dado("que o jogador possui entradas em {string}, {string} e {string}")
    public void datas(String a, String b, String c) {
        for (String data : List.of(a, b, c)) guardar(criar("Sessão", LocalDate.parse(data), false, 0));
    }

    @Quando("o jogador filtra o journal de {string} a {string}")
    public void filtrarDatas(String inicio, String fim) {
        resultado = contexto.obter(() -> consultar.minhas(new FiltroJournal(null, null,
                LocalDate.parse(inicio), LocalDate.parse(fim), null), 1, 20));
    }

    @Quando("o jogador cria uma anotação sem jogo com o texto {string}")
    public void semJogo(String texto) { contexto.executar(() -> guardar(criar(texto, LocalDate.now(), false, 0))); }

    @Dado("entradas de journal sobre jogos e tags diferentes")
    public void diversos() {
        criarComTags("Hades", "exploração"); criarComTags("Hades", "chefes"); criarComTags("Celeste", "exploração");
    }

    @Quando("o jogador filtra o journal pelo jogo {string} e pela tag {string}")
    public void filtroJogoTag(String jogo, String tag) {
        resultado = consultar.minhas(new FiltroJournal(dados.jogo(jogo).id(), tag, null, null, null), 1, 20);
    }

    @Quando("o jogador edita a entrada mantendo apenas a primeira imagem")
    public void removerImagemNaEdicao() {
        var entrada = consultar.minha(entradaId);
        descartada = entrada.anexos().get(1).id();
        gerenciar.editar(entradaId, novosDados(entrada.texto()), List.of(entrada.anexos().getFirst().id()), List.of());
    }

    @Quando("o jogador tenta editar a entrada adicionando uma quarta imagem")
    public void edicaoInvalida() {
        contexto.executar(() -> gerenciar.editar(entradaId, novosDados("Texto que não deve ser salvo"), null, List.of(imagem())));
    }

    @Quando("a transação falha após criar uma entrada com imagem")
    public void falhaAposSalvar() {
        donoId = dados.jogadorAtual().id();
        imagensAntes = jdbc.queryForObject("select count(*) from journal_anexos", Long.class);
        assertThrows(IllegalStateException.class, () -> transacao.executeWithoutResult(status -> {
            criar("Não pode persistir", LocalDate.now(), false, 1);
            jdbc.execute("select 1");
            throw new IllegalStateException("Falha simulada depois da gravação");
        }));
    }

    @Entao("o journal do jogador deve ter {int} entrada")
    public void contar(int quantidade) {
        assertNull(contexto.erro()); assertEquals(quantidade, consultar.minhas(FiltroJournal.todos(), 1, 20).totalItens());
    }

    @Entao("a entrada deve ter as tags {string} e {string}")
    public void tags(String a, String b) { assertEquals(List.of(a, b), consultar.minha(entradaId).tags()); }

    @Entao("a entrada deve ter o texto {string}")
    public void texto(String esperado) { assertEquals(esperado, consultar.minha(entradaId).texto()); }

    @Entao("a entrada deve ter {int} imagens")
    @Entao("a entrada deve ter {int} imagem")
    public void imagens(int quantidade) { assertEquals(quantidade, consultar.minha(entradaId).anexos().size()); }

    @Entao("não deve existir nenhum anexo vinculado à entrada")
    public void semAnexos() {
        assertEquals(0L, jdbc.queryForObject("select count(*) from journal_anexos where entrada_id = ?", Long.class, entradaId));
        assertEquals(0L, jdbc.queryForObject("select count(*) from journal_tags where entrada_id = ?", Long.class, entradaId));
    }

    @Entao("o texto da entrada deve estar mascarado")
    public void mascarado() { assertTrue(publica.mascarada()); assertNull(publica.texto()); }

    @Entao("o texto completo da entrada deve ser exibido")
    public void textoRevelado() { assertFalse(publica.mascarada()); assertEquals("O segredo do final", publica.texto()); }

    @Entao("a consulta deve retornar {int} entradas")
    @Entao("a consulta deve retornar {int} entrada")
    public void quantidade(int total) { assertNull(contexto.erro()); assertEquals(total, resultado.totalItens()); }

    @Entao("a entrada não deve ter jogo associado")
    public void nenhumJogo() { assertNull(consultar.minha(entradaId).jogoId()); }

    @Entao("os dados da imagem descartada devem ser apagados")
    public void semImagemDescartada() {
        assertEquals(0L, jdbc.queryForObject("select count(*) from journal_anexos where id = ?", Long.class, descartada));
    }

    @Entao("o texto original do journal deve ser preservado")
    public void textoPreservado() { texto(textoOriginal); }

    @Entao("a resposta pública não deve incluir os anexos do spoiler")
    public void semImagensPublicas() { assertTrue(publica.anexos().isEmpty()); }

    @Entao("nem a entrada nem a imagem devem ser gravadas")
    public void rollback() {
        assertEquals(0L, consultar.minhas(FiltroJournal.todos(), 1, 20).totalItens());
        assertEquals(imagensAntes, jdbc.queryForObject("select count(*) from journal_anexos", Long.class));
    }

    @Entao("a anotação criada deve aparecer na timeline unificada")
    public void timelineIncluiManual() {
        assertTrue(timeline.consultar(1, 20).itens().stream().anyMatch(e -> e.id().equals(entradaId)));
    }

    private EntradaDeJournal criar(String texto, LocalDate data, boolean spoiler, int imagens) {
        return gerenciar.criar(new DadosEntrada(null, data, "Sessão de jogo", texto, List.of(), spoiler),
                IntStream.range(0, imagens).mapToObj(i -> imagem()).toList());
    }

    private DadosEntrada novosDados(String texto) {
        return new DadosEntrada(null, LocalDate.now(), "Sessão de jogo", texto, List.of(), false);
    }

    private void guardar(EntradaDeJournal entrada) {
        entradaId = entrada.id(); donoId = entrada.donoId(); textoOriginal = entrada.texto();
    }

    private static ImagemDoJournal imagem() {
        var imagem = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 32; x++) for (int y = 0; y < 32; y++) imagem.setRGB(x, y, (x * 131071) ^ (y * 7919));
        try (var bytes = new ByteArrayOutputStream()) {
            ImageIO.write(imagem, "png", bytes);
            return new ImagemDoJournal("sessao.png", "image/png", bytes.toByteArray());
        } catch (IOException erro) { throw new UncheckedIOException(erro); }
    }
}
