package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;

import com.g4.game_tracker_backend.aplicacao.journal.ConsultarJournal;
import com.g4.game_tracker_backend.aplicacao.journal.GerenciarJournal;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.aplicacao.moderacao.ModeracaoOperacoes;
import com.g4.game_tracker_backend.apresentacao.forum.ForumController;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForumRepositorio;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournal;
import com.g4.game_tracker_backend.dominio.journal.EntradaDeJournalRepositorio;
import com.g4.game_tracker_backend.dominio.journal.FiltroJournal;
import com.g4.game_tracker_backend.dominio.moderacao.AcaoModeracao;
import com.g4.game_tracker_backend.dominio.moderacao.DenunciaRepositorio;
import com.g4.game_tracker_backend.dominio.moderacao.LogModeracaoRepositorio;
import com.g4.game_tracker_backend.dominio.moderacao.StatusDenuncia;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;
import com.g4.game_tracker_backend.dominio.notificacao.NotificacaoRepositorio;
import com.g4.game_tracker_backend.dominio.notificacao.TipoNotificacao;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/** Steps da F-14, no mesmo banco H2 e com o mesmo usuário autenticado das outras funcionalidades. */
public class PassosModeracao {

    private final DadosDoCenario dados;
    private final ContextoDoCenario contexto;
    private final UsuarioRepositorio usuarios;
    private final TopicoDeForumRepositorio topicos;
    private final EntradaDeJournalRepositorio journal;
    private final ConsultarJournal consultarJournal;
    private final GerenciarJournal gerenciarJournal;
    private final ModeracaoOperacoes moderacao;
    private final EstadoPublico estados;
    private final DenunciaRepositorio denuncias;
    private final LogModeracaoRepositorio logs;
    private final NotificacaoRepositorio notificacoes;
    private final ForumController forum;
    private final JdbcTemplate jdbc;

    private TipoConteudo tipo;
    private UUID alvo;
    private UUID topicoId;
    private Usuario autor;
    private UUID denunciaId;
    private UUID logId;

    public PassosModeracao(DadosDoCenario dados, ContextoDoCenario contexto, UsuarioRepositorio usuarios,
            TopicoDeForumRepositorio topicos, EntradaDeJournalRepositorio journal, ConsultarJournal consultarJournal,
            GerenciarJournal gerenciarJournal, ModeracaoOperacoes moderacao, EstadoPublico estados,
            DenunciaRepositorio denuncias, LogModeracaoRepositorio logs, NotificacaoRepositorio notificacoes,
            ForumController forum, JdbcTemplate jdbc) {
        this.dados = dados;
        this.contexto = contexto;
        this.usuarios = usuarios;
        this.topicos = topicos;
        this.journal = journal;
        this.consultarJournal = consultarJournal;
        this.gerenciarJournal = gerenciarJournal;
        this.moderacao = moderacao;
        this.estados = estados;
        this.denuncias = denuncias;
        this.logs = logs;
        this.notificacoes = notificacoes;
        this.forum = forum;
        this.jdbc = jdbc;
    }

    // ---- Situação inicial ----

    @Dado("que há um jogador autenticado para moderação")
    public void jogador() {
        dados.autenticar("denunciante da moderação");
    }

    @Dado("que há um moderador autenticado para moderação")
    public void moderador() {
        Usuario moderador = usuarios.salvar(Usuario.reconstituir(UUID.randomUUID(), "Moderador",
                UUID.randomUUID() + "@bdd.local", "hash", Papel.MODERADOR, true));
        SessaoDeTeste.autenticarComo(moderador);
    }

    @Dado("que existe conteúdo moderável do tipo {string}")
    public void alvo(String nome) {
        tipo = TipoConteudo.valueOf(nome);
        autor = dados.jogador("autor moderável");
        UUID autorId = autor.id();
        alvo = UUID.randomUUID();
        Instant agora = Instant.now();
        switch (tipo) {
            case TOPICO, RESPOSTA -> {
                TopicoDeForum topico = TopicoDeForum.criar(autorId, null, "Geral", "Tópico alvo", "Texto alvo", agora);
                if (tipo == TipoConteudo.RESPOSTA) {
                    topico = topico.responder(autorId, null, "Resposta alvo", agora.plusSeconds(1));
                    alvo = topico.ultimaResposta().id();
                }
                topicoId = topicos.salvar(topico).id();
                if (tipo == TipoConteudo.TOPICO) {
                    alvo = topicoId;
                }
            }
            case PERFIL -> alvo = autorId;
            case REVIEW -> jdbc.update("insert into reviews(id,autor_id,jogo_id,nota_geral,texto,criada_em,"
                    + "atualizada_em) values (?,?,?,?,?,?,?)", alvo, autorId, UUID.randomUUID(), 4, "Review alvo",
                    agora, agora);
            case LISTA -> jdbc.update("insert into listas_personalizadas(id,versao,dono_id,titulo,titulo_chave,"
                    + "descricao,criada_em,atualizada_em) values (?,?,?,?,?,?,?,?)", alvo, 0L, autorId,
                    "Lista alvo " + alvo, "lista " + alvo, "Descrição alvo", agora, agora);
            case AVALIACAO_MARKETPLACE -> jdbc.update("insert into marketplace_avaliacoes(id,pedido_id,avaliador_id,"
                    + "avaliado_id,papel_avaliado,estrelas,comentario,criada_em) values (?,?,?,?,?,?,?,?)", alvo,
                    UUID.randomUUID(), autorId, UUID.randomUUID(), "VENDEDOR", 5, "Avaliação alvo", agora);
            case ANUNCIO -> jdbc.update("insert into anuncios_marketplace(id,vendedor_id,jogo_id,titulo,estado,preco,"
                    + "tipo_negociacao,midia,localidade,status,criado_em,atualizado_em)"
                    + " values (?,?,?,?,?,?,?,?,?,?,?,?)", alvo, autorId, UUID.randomUUID(), "Anúncio alvo", "NOVO",
                    100, "VENDA", "FISICO", "Recife", "ATIVO", agora, agora);
            case JOURNAL -> alvo = journal.salvar(EntradaDeJournal.criarManual(autorId, null, agora,
                    "Sessão alvo", "Texto da sessão alvo", List.of(), false, agora)).id();
        }
    }

    @Dado("que existe resposta com duas filhas para moderação")
    public void respostaComFilhas() {
        tipo = TipoConteudo.RESPOSTA;
        UUID autorId = dados.jogador("autor das respostas moderadas").id();
        TopicoDeForum topico = TopicoDeForum.criar(autorId, null, "Geral", "Conversa", "Texto", Instant.now());
        topico = topico.responder(autorId, null, "Resposta principal", Instant.now());
        alvo = topico.ultimaResposta().id();
        topico = topico.responder(autorId, alvo, "Filha um", Instant.now().plusSeconds(1));
        topico = topico.responder(autorId, alvo, "Filha dois", Instant.now().plusSeconds(2));
        topicoId = topicos.salvar(topico).id();
    }

    @Dado("que ele denunciou o conteúdo")
    public void jaDenunciou() {
        denunciar("Motivo de teste");
    }

    // ---- Ações ----

    @Quando("ele denuncia o conteúdo com motivo {string}")
    public void denunciar(String motivo) {
        denunciaId = moderacao.denunciar(tipo, alvo, motivo).id();
    }

    @Quando("ele denuncia o conteúdo duas vezes")
    public void denunciaDuasVezes() {
        denunciar("Duplicada");
        moderacao.denunciar(tipo, alvo, "Duplicada");
    }

    @Quando("tenta consultar a fila da moderação")
    public void consultarFila() {
        contexto.obter(() -> moderacao.fila(1, 10));
    }

    @Quando("tenta ocultar sem justificativa")
    public void semJustificativa() {
        contexto.obter(() -> moderacao.agir(AcaoModeracao.OCULTAR, tipo, alvo, "", null, null, null));
    }

    @Quando("um moderador oculta o conteúdo com justificativa {string}")
    public void ocultar(String motivo) {
        moderador();
        logId = moderacao.agir(AcaoModeracao.OCULTAR, tipo, alvo, motivo, null, null, null).id();
    }

    @Quando("um moderador arquiva a denúncia")
    public void arquivar() {
        moderador();
        logId = moderacao.arquivar(denunciaId, "Improcedente").id();
    }

    @Quando("o moderador remove a resposta com justificativa {string}")
    public void removerResposta(String motivo) {
        remover(motivo);
    }

    @Quando("o moderador remove o tópico com justificativa {string}")
    public void removerTopico(String motivo) {
        remover(motivo);
    }

    @Quando("o moderador remove o conteúdo com justificativa {string}")
    public void remover(String motivo) {
        logId = moderacao.agir(AcaoModeracao.EXCLUIR, tipo, alvo, motivo, null, null, null).id();
    }

    @Quando("oculta e reverte a ocultação do conteúdo")
    public void reverter() {
        moderacao.agir(AcaoModeracao.OCULTAR, tipo, alvo, "Ocultar", null, null, null);
        moderacao.agir(AcaoModeracao.REVERTER_OCULTACAO, tipo, alvo, "Revisado", null, null, null);
    }

    @Quando("aplica aviso 18+ ao conteúdo")
    public void aviso18() {
        moderacao.agir(AcaoModeracao.MARCAR_18, tipo, alvo, "Sensível", null, null, null);
    }

    @Quando("adverte o autor por {string}")
    public void advertir(String motivo) {
        moderacao.advertir(autor.id(), motivo);
    }

    // ---- Verificações ----

    @Entao("existe uma denúncia pendente e o conteúdo continua público")
    public void pendenteVisivel() {
        assertEquals(StatusDenuncia.PENDENTE, denuncias.buscarPorId(denunciaId).orElseThrow().status());
        assertTrue(estados.visivel(tipo, alvo));
    }

    @Entao("há uma única denúncia pendente do jogador para o alvo")
    public void umaSo() {
        assertEquals(1, denuncias.listarPendentesDoAlvo(tipo, alvo).size());
    }

    @Entao("o acesso à moderação é recusado")
    public void acessoRecusado() {
        assertNotNull(contexto.erro());
        assertEquals("Acesso restrito a moderadores e administradores", contexto.erro().getMessage());
    }

    @Entao("as denúncias do alvo ficam procedentes e há um log imutável")
    public void procedenteLog() {
        assertEquals(StatusDenuncia.PROCEDENTE, denuncias.buscarPorId(denunciaId).orElseThrow().status());
        assertEquals(AcaoModeracao.OCULTAR, logs.buscarPorId(logId).orElseThrow().acao());
    }

    @Entao("o autor recebe a notificação da ação")
    public void notificacao() {
        assertTrue(notificacoes.existePorOrigem("moderacao:" + logId));
    }

    @Entao("a denúncia fica improcedente e há um log de arquivamento")
    public void improcedente() {
        assertEquals(StatusDenuncia.IMPROCEDENTE, denuncias.buscarPorId(denunciaId).orElseThrow().status());
        assertEquals(AcaoModeracao.ARQUIVAR_DENUNCIA, logs.buscarPorId(logId).orElseThrow().acao());
    }

    @Entao("as duas filhas continuam visíveis e o contador exclui a resposta")
    public void filhasVisiveis() {
        assertEquals(3, forum.consultar(topicoId).respostas().size());
        assertEquals(2, forum.consultar(topicoId).totalRespostas());
        assertEquals(2, jdbc.queryForObject("select total_respostas from forum_topicos where id = ?", Integer.class,
                topicoId));
    }

    @Entao("o endereço do tópico mostra a mensagem de sistema e status {string}")
    public void topicoSistema(String status) {
        var topico = forum.consultar(alvo);
        assertEquals(status, topico.status());
        assertEquals(ModeracaoOperacoes.MENSAGEM_REMOCAO, topico.titulo());
        assertEquals(ModeracaoOperacoes.MENSAGEM_REMOCAO, topico.conteudo());
    }

    @Entao("o conteúdo volta a ser público e existem dois logs")
    public void doisLogs() {
        assertTrue(estados.visivel(tipo, alvo));
        assertEquals(2, logs.listarPorConteudo(alvo).size());
    }

    @Entao("o conteúdo permanece público com aviso 18+")
    public void avisoPublico() {
        assertTrue(estados.visivel(tipo, alvo));
        assertTrue(estados.aviso18(tipo, alvo));
    }

    @Entao("a contagem de advertências é 1 e o autor é notificado")
    public void advertido() {
        assertEquals(1, moderacao.totalAdvertencias(autor.id()));
        assertTrue(notificacoes.listarPorUsuario(autor.id()).stream()
                .anyMatch(n -> n.tipo() == TipoNotificacao.ADVERTENCIA));
    }

    @Entao("a entrada não aparece no journal público do autor")
    public void foraDoJournalPublico() {
        var publicas = consultarJournal.publicas(autor.id(), FiltroJournal.todos(), 1, 20);
        assertTrue(publicas.itens().stream().noneMatch(entrada -> entrada.id().equals(alvo)));
        contexto.obter(() -> consultarJournal.publica(autor.id(), alvo, false));
        assertInstanceOf(RecursoNaoEncontradoException.class, contexto.erro());
    }

    @Entao("o autor não consegue mais editar a entrada")
    public void edicaoBloqueada() {
        SessaoDeTeste.autenticarComo(autor);
        var dadosNovos = new GerenciarJournal.DadosEntrada(null, LocalDate.now(), "Outro título", "Outro texto",
                List.of(), false);
        contexto.obter(() -> gerenciarJournal.editar(alvo, dadosNovos, null, List.of()));
        assertNotNull(contexto.erro());
        assertEquals("Conteúdo indisponível", contexto.erro().getMessage());
    }

    @Entao("o autor vê a entrada com a mensagem de sistema")
    public void autorVeMensagem() {
        SessaoDeTeste.autenticarComo(autor);
        var entrada = consultarJournal.minha(alvo);
        assertEquals(ModeracaoOperacoes.MENSAGEM_REMOCAO, entrada.texto());
        assertTrue(entrada.moderada());
        assertFalse(entrada.editavel());
        assertTrue(entrada.anexos().isEmpty());
    }
}
