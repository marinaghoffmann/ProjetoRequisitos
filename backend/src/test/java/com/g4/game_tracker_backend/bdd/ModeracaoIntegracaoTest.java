package com.g4.game_tracker_backend.bdd;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import com.g4.game_tracker_backend.infraestrutura.configuracao.ProtecaoMidiaModerada;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import com.g4.game_tracker_backend.aplicacao.moderacao.*;
import com.g4.game_tracker_backend.dominio.compartilhado.AcessoNegadoException;
import com.g4.game_tracker_backend.dominio.forum.*;
import com.g4.game_tracker_backend.dominio.identidade.*;
import com.g4.game_tracker_backend.dominio.moderacao.*;
import com.g4.game_tracker_backend.dominio.notificacao.NotificacaoRepositorio;
@SpringBootTest @ActiveProfiles("test")
class ModeracaoIntegracaoTest {
 @Autowired ModeracaoOperacoes moderacao;
 @Autowired EstadoPublico estados;
 @Autowired TopicoDeForumRepositorio topicos;
 @Autowired UsuarioRepositorio usuarios;
 @Autowired NotificacaoRepositorio notificacoes;
 @Autowired JdbcTemplate jdbc;
 @Autowired ProtecaoMidiaModerada filtroMidias;
 @AfterEach void sair() { SecurityContextHolder.clearContext(); }
 private Usuario usuario(Papel papel) { return usuarios.salvar(Usuario.reconstituir(UUID.randomUUID(),"Pessoa",UUID.randomUUID()+"@teste.local","hash",papel,true)); }
 @Test void denunciaRemocaoReversaoELog() {
  Usuario autor=usuario(Papel.COMUM), denunciante=usuario(Papel.COMUM), mod=usuario(Papel.MODERADOR);
  TopicoDeForum topico=TopicoDeForum.criar(autor.id(),null,"Geral","Tópico moderado","Conteúdo",Instant.now());
  topico=topico.responder(autor.id(),null,"Resposta ofensiva",Instant.now());
  UUID resposta=topico.ultimaResposta().id();
  topico=topico.responder(autor.id(),resposta,"Resposta filha",Instant.now().plusSeconds(1));
  UUID topicoId=topicos.salvar(topico).id();
  SessaoDeTeste.autenticarComo(denunciante);
  var primeira=moderacao.denunciar(TipoConteudo.RESPOSTA,resposta,"Ofensa");
  assertEquals(primeira.id(),moderacao.denunciar(TipoConteudo.RESPOSTA,resposta,"Ofensa").id());
  assertThrows(AcessoNegadoException.class,()->moderacao.fila(1,10));
  SessaoDeTeste.autenticarComo(mod);
  assertTrue(moderacao.fila(1,50).itens().stream().anyMatch(d->d.id().equals(primeira.id())));
  assertEquals("A justificativa é obrigatória",assertThrows(RuntimeException.class,()->moderacao.agir(AcaoModeracao.EXCLUIR,TipoConteudo.RESPOSTA,resposta,"",null,null,null)).getMessage());
  var log=moderacao.agir(AcaoModeracao.EXCLUIR,TipoConteudo.RESPOSTA,resposta,"Spam",null,null,null);
  assertFalse(estados.visivel(TipoConteudo.RESPOSTA,resposta));
  assertEquals(1,jdbc.queryForObject("select total_respostas from forum_topicos where id = ?",Integer.class,topicoId));
  assertEquals(2,topicos.buscarPorId(topicoId).orElseThrow().respostas().size());
  assertEquals("PROCEDENTE",jdbc.queryForObject("select status from moderacao_denuncias where id = ?",String.class,primeira.id()));
  assertTrue(notificacoes.existePorOrigem("moderacao:"+log.id()));
  moderacao.agir(AcaoModeracao.REVERTER_EXCLUSAO,TipoConteudo.RESPOSTA,resposta,"Decisão revista",null,null,null);
  assertTrue(estados.visivel(TipoConteudo.RESPOSTA,resposta));
  assertEquals(2,jdbc.queryForObject("select total_respostas from forum_topicos where id = ?",Integer.class,topicoId));
  assertTrue(moderacao.historico(1,50).itens().stream().anyMatch(l->l.id().equals(log.id())));
 }
 @Test void aviso18EArquivamentoSaoAuditados() {
  Usuario autor=usuario(Papel.COMUM), denunciante=usuario(Papel.COMUM), mod=usuario(Papel.MODERADOR);
  SessaoDeTeste.autenticarComo(denunciante);
  var denuncia=moderacao.denunciar(TipoConteudo.PERFIL,autor.id(),"Perfil inadequado");
  SessaoDeTeste.autenticarComo(mod);
  moderacao.arquivar(denuncia.id(),"Não viola regras");
  assertEquals("IMPROCEDENTE",jdbc.queryForObject("select status from moderacao_denuncias where id = ?",String.class,denuncia.id()));
  moderacao.agir(AcaoModeracao.MARCAR_18,TipoConteudo.PERFIL,autor.id(),"Conteúdo sensível",null,null,null);
  assertTrue(estados.aviso18(TipoConteudo.PERFIL,autor.id()));
  assertTrue(estados.visivel(TipoConteudo.PERFIL,autor.id()));
  moderacao.agir(AcaoModeracao.RETIRAR_18,TipoConteudo.PERFIL,autor.id(),"Revisto",null,null,null);
  assertFalse(estados.aviso18(TipoConteudo.PERFIL,autor.id()));
  moderacao.advertir(autor.id(),"Conduta inadequada");
  assertEquals(1,moderacao.totalAdvertencias(autor.id()));
 }
 @Test void fotoAntigaDoPerfilModeradoNaoVazaPorUrlDireta() throws Exception {
  Usuario autor=usuarios.salvar(usuario(Papel.COMUM).comAvatar("/uploads/perfil/avatar/foto-antiga.png"));
  Usuario mod=usuario(Papel.MODERADOR);SessaoDeTeste.autenticarComo(mod);
  moderacao.agir(AcaoModeracao.OCULTAR,TipoConteudo.PERFIL,autor.id(),"Foto imprópria",null,null,null);
  usuarios.salvar(autor.comAvatar("/uploads/perfil/avatar/foto-nova.png"));
  MockHttpServletRequest requisicao=new MockHttpServletRequest("GET","/uploads/perfil/avatar/foto-antiga.png");
  MockHttpServletResponse resposta=new MockHttpServletResponse();
  filtroMidias.doFilter(requisicao,resposta,(req,res)->((jakarta.servlet.http.HttpServletResponse)res).setStatus(200));
  assertEquals(404,resposta.getStatus());
 }
 @Test void decisoesSimultaneasNaoPerdemEstadoNemAuditoria() throws Exception {
  Usuario autor=usuario(Papel.COMUM),modA=usuario(Papel.MODERADOR),modB=usuario(Papel.ADMINISTRADOR);
  UUID id=topicos.salvar(TopicoDeForum.criar(autor.id(),null,"Geral","Concorrência","Texto",Instant.now())).id();
  CountDownLatch inicio=new CountDownLatch(1);
  try(var executor=Executors.newFixedThreadPool(2)) {
   Future<?> primeira=executor.submit(()->{ SessaoDeTeste.autenticarComo(modA);try { inicio.await(); moderacao.agir(AcaoModeracao.OCULTAR,TipoConteudo.TOPICO,id,"Ocultar",null,null,null); } catch(InterruptedException e) { Thread.currentThread().interrupt(); } finally { SecurityContextHolder.clearContext(); } });
   Future<?> segunda=executor.submit(()->{ SessaoDeTeste.autenticarComo(modB);try { inicio.await(); moderacao.agir(AcaoModeracao.EXCLUIR,TipoConteudo.TOPICO,id,"Remover",null,null,null); } catch(InterruptedException e) { Thread.currentThread().interrupt(); } finally { SecurityContextHolder.clearContext(); } });
   inicio.countDown();primeira.get(20,TimeUnit.SECONDS);segunda.get(20,TimeUnit.SECONDS);
  }
  SessaoDeTeste.autenticarComo(modA);
  assertTrue(estados.oculto(TipoConteudo.TOPICO,id));assertTrue(estados.removido(TipoConteudo.TOPICO,id));
  assertEquals(2,moderacao.historico(1,50).itens().stream().filter(l->id.equals(l.conteudoId())).count());
 }

 @Test void moverRespostaMantemArvoreERejeitaCiclo() {
  Usuario autor=usuario(Papel.COMUM),mod=usuario(Papel.MODERADOR);
  TopicoDeForum topico=TopicoDeForum.criar(autor.id(),null,"Geral","Mover resposta","Texto",Instant.now());
  topico=topico.responder(autor.id(),null,"Primeira",Instant.now());UUID primeira=topico.ultimaResposta().id();
  topico=topico.responder(autor.id(),null,"Segunda",Instant.now().plusSeconds(1));UUID segunda=topico.ultimaResposta().id();
  UUID topicoId=topicos.salvar(topico).id();SessaoDeTeste.autenticarComo(mod);
  moderacao.agir(AcaoModeracao.MOVER,TipoConteudo.RESPOSTA,primeira,"Organizar",null,null,null,segunda);
  assertEquals(segunda,topicos.buscarPorId(topicoId).orElseThrow().resposta(primeira).respostaPaiId());
  assertEquals(2,jdbc.queryForObject("select total_respostas from forum_topicos where id = ?",Integer.class,topicoId));
  assertEquals("A movimentação criaria um ciclo de respostas",assertThrows(RuntimeException.class,()->
    moderacao.agir(AcaoModeracao.MOVER,TipoConteudo.RESPOSTA,segunda,"Organizar",null,null,null,primeira)).getMessage());
 }

}
