package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.aplicacao.moderacao.*;
import com.g4.game_tracker_backend.apresentacao.forum.ForumController;
import com.g4.game_tracker_backend.dominio.forum.*;
import com.g4.game_tracker_backend.dominio.identidade.*;
import com.g4.game_tracker_backend.dominio.moderacao.*;
import com.g4.game_tracker_backend.dominio.notificacao.NotificacaoRepositorio;
import com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao.*;
import io.cucumber.java.pt.*;

/** F-14 steps use the same H2 schema and authenticated principal as other BDD features. */
public class PassosModeracao {
 private final DadosDoCenario dados;
 private final ContextoDoCenario contexto;
 private final VerificadorDeAcesso acesso;
 private final UsuarioRepositorio usuarios;
 private final TopicoDeForumRepositorio topicos;
 private final ModeracaoOperacoes moderacao;
 private final EstadoPublico estados;
 private final DenunciaRepository denuncias;
 private final LogModeracaoRepository logs;
 private final NotificacaoRepositorio notificacoes;
 private final ForumController forum;
 private final JdbcTemplate jdbc;
 private TipoConteudo tipo;
 private UUID alvo,topicoId,autorId,denunciaId;
 private UUID logId;

 public PassosModeracao(DadosDoCenario dados,ContextoDoCenario contexto,VerificadorDeAcesso acesso,
  UsuarioRepositorio usuarios,TopicoDeForumRepositorio topicos,ModeracaoOperacoes moderacao,EstadoPublico estados,
  DenunciaRepository denuncias,LogModeracaoRepository logs,NotificacaoRepositorio notificacoes,
  ForumController forum,JdbcTemplate jdbc) {
  this.dados=dados;this.contexto=contexto;this.acesso=acesso;this.usuarios=usuarios;this.topicos=topicos;
  this.moderacao=moderacao;this.estados=estados;this.denuncias=denuncias;this.logs=logs;this.notificacoes=notificacoes;
  this.forum=forum;this.jdbc=jdbc;
 }
 @Dado("que há um jogador autenticado para moderação")
 public void jogador() { dados.autenticar("denunciante da moderação"); }
 @Dado("que há um moderador autenticado para moderação")
 public void moderador() {
  Usuario mod=usuarios.salvar(Usuario.reconstituir(UUID.randomUUID(),"Moderador",UUID.randomUUID()+"@bdd.local","hash",Papel.MODERADOR,true));
  SessaoDeTeste.autenticarComo(mod);
 }
 @Dado("que existe conteúdo moderável do tipo {string}")
 public void alvo(String nome) {
  tipo=TipoConteudo.valueOf(nome); autorId=dados.jogador("autor moderável").id(); alvo=UUID.randomUUID();
  Instant agora=Instant.now();
  switch(tipo) {
   case TOPICO,RESPOSTA -> {
    TopicoDeForum t=TopicoDeForum.criar(autorId,null,"Geral","Tópico alvo","Texto alvo",agora);
    if(tipo==TipoConteudo.RESPOSTA) { t=t.responder(autorId,null,"Resposta alvo",agora.plusSeconds(1)); alvo=t.ultimaResposta().id(); }
    topicoId=topicos.salvar(t).id(); if(tipo==TipoConteudo.TOPICO) alvo=topicoId;
   }
   case PERFIL -> alvo=autorId;
   case REVIEW -> jdbc.update("insert into reviews(id,autor_id,jogo_id,nota_geral,texto,criada_em,atualizada_em) values (?,?,?,?,?,?,?)",alvo,autorId,UUID.randomUUID(),4,"Review alvo",agora,agora);
   case LISTA -> jdbc.update("insert into listas_personalizadas(id,versao,dono_id,titulo,titulo_chave,descricao,criada_em,atualizada_em) values (?,?,?,?,?,?,?,?)",alvo,0L,autorId,"Lista alvo "+alvo,"lista "+alvo,"Descrição alvo",agora,agora);
   case AVALIACAO_MARKETPLACE -> jdbc.update("insert into marketplace_avaliacoes(id,pedido_id,avaliador_id,avaliado_id,papel_avaliado,estrelas,comentario,criada_em) values (?,?,?,?,?,?,?,?)",alvo,UUID.randomUUID(),autorId,UUID.randomUUID(),"VENDEDOR",5,"Avaliação alvo",agora);
   case ANUNCIO -> jdbc.update("insert into anuncios_marketplace(id,vendedor_id,jogo_id,titulo,estado,preco,tipo_negociacao,midia,localidade,status,criado_em,atualizado_em) values (?,?,?,?,?,?,?,?,?,?,?,?)",alvo,autorId,UUID.randomUUID(),"Anúncio alvo","NOVO",100,"VENDA","FISICO","Recife","ATIVO",agora,agora);
  }
 }
 @Dado("que existe resposta com duas filhas para moderação")
 public void respostaComFilhas() {
  tipo=TipoConteudo.RESPOSTA;autorId=dados.jogador("autor das respostas moderadas").id();
  TopicoDeForum t=TopicoDeForum.criar(autorId,null,"Geral","Conversa","Texto",Instant.now());
  t=t.responder(autorId,null,"Resposta principal",Instant.now());alvo=t.ultimaResposta().id();
  t=t.responder(autorId,alvo,"Filha um",Instant.now().plusSeconds(1));
  t=t.responder(autorId,alvo,"Filha dois",Instant.now().plusSeconds(2));topicoId=topicos.salvar(t).id();
 }
 @Quando("ele denuncia o conteúdo com motivo {string}")
 public void denunciar(String motivo) { denunciaId=moderacao.denunciar(tipo,alvo,motivo).id(); }
 @Dado("que ele denunciou o conteúdo")
 public void jaDenunciou() { denunciar("Motivo de teste"); }
 @Quando("ele denuncia o conteúdo duas vezes")
 public void denunciaDuasVezes() { denunciar("Duplicada"); moderacao.denunciar(tipo,alvo,"Duplicada"); }
 @Entao("existe uma denúncia pendente e o conteúdo continua público")
 public void pendenteVisivel() { assertEquals(StatusDenuncia.PENDENTE,denuncias.findById(denunciaId).orElseThrow().status);assertTrue(estados.visivel(tipo,alvo)); }
 @Entao("há uma única denúncia pendente do jogador para o alvo")
 public void umaSo() { assertEquals(1,denuncias.findByTipoAndConteudoIdAndStatus(tipo,alvo,StatusDenuncia.PENDENTE).size()); }
 @Quando("tenta consultar a fila da moderação")
 public void consultarFila() { contexto.obter(()->moderacao.fila(1,10)); }
 @Entao("o acesso à moderação é recusado")
 public void acessoRecusado() { assertNotNull(contexto.erro());assertEquals("Acesso restrito a moderadores e administradores",contexto.erro().getMessage()); }
 @Quando("tenta ocultar sem justificativa")
 public void semJustificativa() { contexto.obter(()->moderacao.agir(AcaoModeracao.OCULTAR,tipo,alvo,"",null,null,null)); }
 @Quando("um moderador oculta o conteúdo com justificativa {string}")
 public void ocultar(String motivo) { moderador();logId=moderacao.agir(AcaoModeracao.OCULTAR,tipo,alvo,motivo,null,null,null).id(); }
 @Entao("as denúncias do alvo ficam procedentes e há um log imutável")
 public void procedenteLog() { assertEquals(StatusDenuncia.PROCEDENTE,denuncias.findById(denunciaId).orElseThrow().status);assertEquals(AcaoModeracao.OCULTAR,logs.findById(logId).orElseThrow().acao); }
 @Entao("o autor recebe a notificação da ação")
 public void notificacao() { assertTrue(notificacoes.existePorOrigem("moderacao:"+logId)); }
 @Quando("um moderador arquiva a denúncia")
 public void arquivar() { moderador();logId=moderacao.arquivar(denunciaId,"Improcedente").id(); }
 @Entao("a denúncia fica improcedente e há um log de arquivamento")
 public void improcedente() { assertEquals(StatusDenuncia.IMPROCEDENTE,denuncias.findById(denunciaId).orElseThrow().status);assertEquals(AcaoModeracao.ARQUIVAR_DENUNCIA,logs.findById(logId).orElseThrow().acao); }
 @Quando("o moderador remove a resposta com justificativa {string}")
 public void removerResposta(String motivo) { logId=moderacao.agir(AcaoModeracao.EXCLUIR,tipo,alvo,motivo,null,null,null).id(); }
 @Entao("as duas filhas continuam visíveis e o contador exclui a resposta")
 public void filhasVisiveis() { assertEquals(3,forum.consultar(topicoId).respostas().size());assertEquals(2,forum.consultar(topicoId).totalRespostas());assertEquals(2,jdbc.queryForObject("select total_respostas from forum_topicos where id = ?",Integer.class,topicoId)); }
 @Quando("o moderador remove o tópico com justificativa {string}")
 public void removerTopico(String motivo) { logId=moderacao.agir(AcaoModeracao.EXCLUIR,tipo,alvo,motivo,null,null,null).id(); }
 @Entao("o endereço do tópico mostra a mensagem de sistema e status {string}")
 public void topicoSistema(String status) { var t=forum.consultar(alvo);assertEquals(status,t.status());assertEquals(ModeracaoOperacoes.MENSAGEM_REMOCAO,t.titulo());assertEquals(ModeracaoOperacoes.MENSAGEM_REMOCAO,t.conteudo()); }
 @Quando("oculta e reverte a ocultação do conteúdo")
 public void reverter() { moderacao.agir(AcaoModeracao.OCULTAR,tipo,alvo,"Ocultar",null,null,null);moderacao.agir(AcaoModeracao.REVERTER_OCULTACAO,tipo,alvo,"Revisado",null,null,null); }
 @Entao("o conteúdo volta a ser público e existem dois logs")
 public void doisLogs() { assertTrue(estados.visivel(tipo,alvo));assertEquals(2,logs.findAll().stream().filter(l->alvo.equals(l.conteudoId)).count()); }
 @Quando("aplica aviso 18+ ao conteúdo")
 public void aviso18() { moderacao.agir(AcaoModeracao.MARCAR_18,tipo,alvo,"Sensível",null,null,null); }
 @Entao("o conteúdo permanece público com aviso 18+")
 public void avisoPublico() { assertTrue(estados.visivel(tipo,alvo));assertTrue(estados.aviso18(tipo,alvo)); }
 @Quando("adverte o autor por {string}")
 public void advertir(String motivo) { moderacao.advertir(autorId,motivo); }
 @Entao("a contagem de advertências é 1 e o autor é notificado")
 public void advertido() { assertEquals(1,moderacao.totalAdvertencias(autorId));assertTrue(notificacoes.listarPorUsuario(autorId).stream().anyMatch(n->n.tipo()==com.g4.game_tracker_backend.dominio.notificacao.TipoNotificacao.ADVERTENCIA)); }
}
