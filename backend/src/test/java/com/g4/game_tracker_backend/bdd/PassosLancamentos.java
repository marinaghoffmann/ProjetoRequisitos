package com.g4.game_tracker_backend.bdd;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.aplicacao.catalogo.AtualizarJogoManual;
import com.g4.game_tracker_backend.aplicacao.catalogo.ImportarJogo;
import com.g4.game_tracker_backend.aplicacao.catalogo.DesativarJogo;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.identidade.Papel;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;
import com.g4.game_tracker_backend.aplicacao.lancamentos.*;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.lancamentos.*;
import com.g4.game_tracker_backend.dominio.notificacao.NotificacaoRepositorio;
import com.g4.game_tracker_backend.dominio.notificacao.TipoNotificacao;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Entao;

public class PassosLancamentos {
    private final DadosDoCenario dados;
    private final ContextoDoCenario contexto;
    private final VerificadorDeAcesso acesso;
    private final JogoRepositorio jogos;
    private final LancamentoRepositorio lancamentos;
    private final InteresseLancamentoRepositorio interesses;
    private final AlertaLancamentoRepositorio alertas;
    private final NotificacaoRepositorio notificacoes;
    private final GerenciarLancamentos gestao;
    private final GerenciarInteresseLancamento gestaoInteresse;
    private final ConsultarLancamentos consulta;
    private final ProcessarLancamentos rotina;
    private final AtualizarJogoManual atualizarCatalogo;
    private final ImportarJogo importarJogo;
    private final UsuarioRepositorio usuarios;
    private final DesativarJogo desativarJogo;
    private final Map<String,UUID> ids=new HashMap<>();
    private LancamentoFuturo atual;
    private List<ConsultarLancamentos.Visao> listados;
    private int disparados;
    private int interessados;
    private Instant instanteDaRotina;
    private UUID jogoImportadoId;

    public PassosLancamentos(DadosDoCenario dados, ContextoDoCenario contexto, VerificadorDeAcesso acesso,
            JogoRepositorio jogos, LancamentoRepositorio lancamentos,
            InteresseLancamentoRepositorio interesses, AlertaLancamentoRepositorio alertas,
            NotificacaoRepositorio notificacoes, GerenciarLancamentos gestao,
            GerenciarInteresseLancamento gestaoInteresse, ConsultarLancamentos consulta,
            ProcessarLancamentos rotina, AtualizarJogoManual atualizarCatalogo,
            ImportarJogo importarJogo, UsuarioRepositorio usuarios, DesativarJogo desativarJogo) {
        this.dados=dados;this.contexto=contexto;this.acesso=acesso;this.jogos=jogos;
        this.lancamentos=lancamentos;this.interesses=interesses;this.alertas=alertas;
        this.notificacoes=notificacoes;this.gestao=gestao;this.gestaoInteresse=gestaoInteresse;
        this.consulta=consulta;this.rotina=rotina;this.atualizarCatalogo=atualizarCatalogo;
        this.importarJogo=importarJogo;this.usuarios=usuarios;this.desativarJogo=desativarJogo;
    }
    private LocalDate hoje() { return DatasLancamentos.hoje(Instant.now()); }
    private LancamentoFuturo fixture(String titulo,LocalDate data) {
        UUID jogoId=dados.jogo(titulo).id();
        ids.put(titulo,jogoId);
        jogos.salvar(jogos.buscarPorId(jogoId).orElseThrow().comDataLancamento(data));
        atual=lancamentos.buscarPorJogo(jogoId).orElseGet(()->lancamentos.salvar(
                new LancamentoFuturo(UUID.randomUUID(),jogoId,data,StatusLancamento.FUTURO_LANCAMENTO,
                        Instant.now(),List.of())));
        return atual;
    }
    private LancamentoFuturo recarregar() { return lancamentos.buscar(atual.id()).orElseThrow(); }

    @Quando("o administrador cadastra o lançamento de {string} para daqui a {int} dias")
    public void cadastrarFuturo(String titulo,int dias) {
        UUID jogoId=dados.jogo(titulo).id(); ids.put(titulo,jogoId);
        atual=contexto.obter(()->gestao.cadastrar(jogoId,hoje().plusDays(dias)));
    }
    @Quando("o administrador cadastra o lançamento de {string} para ontem")
    public void cadastrarOntem(String titulo) {
        UUID jogoId=dados.jogo(titulo).id();
        contexto.obter(()->gestao.cadastrar(jogoId,hoje().minusDays(1)));
    }
    @Entao("o lançamento de {string} deve ter status {string}")
    public void status(String titulo,String status) {
        assertNotNull(atual);
        assertEquals(StatusLancamento.valueOf(status),recarregar().status());
    }
    @Dado("que o lançamento de {string} está previsto para hoje")
    public void previstoHoje(String titulo) { fixture(titulo,hoje()); }
    @Dado("que o lançamento de {string} está previsto para daqui a {int} dias")
    public void previstoEmDias(String titulo,int dias) { fixture(titulo,hoje().plusDays(dias)); }
    @Quando("a rotina de transição de lançamentos é executada")
    public void transicionar() { rotina.transicionar(Instant.now()); }
    @Quando("o jogador marca {string} em {string}")
    public void marcar(String acao,String titulo) {
        atual=lancamentos.buscarPorJogo(ids.get(titulo)).orElseThrow();
        contexto.executar(()->gestaoInteresse.marcar(atual.id()));
    }
    @Entao("deve existir uma notificação agendada para 24 horas antes do lançamento de {string}")
    public void alertaAgendado(String titulo) {
        var i=interesses.buscar(dados.jogadorAtual().id(),ids.get(titulo)).orElseThrow();
        assertEquals(DatasLancamentos.dispararEm(recarregar().dataPrevista()),
                alertas.pendentePorInteresse(i.id()).orElseThrow().dispararEm());
    }
    @Dado("que o jogador marcou interesse no lançamento de {string}")
    public void jogadorInteressado(String titulo) {
        fixture(titulo,hoje().plusDays(10)); gestaoInteresse.marcar(atual.id());
    }
    @Quando("o jogador desmarca {string} em {string}")
    public void desmarcar(String acao,String titulo) {
        contexto.executar(()->gestaoInteresse.desmarcar(atual.id()));
    }
    @Entao("o jogador não deve ter alerta ativo para {string}")
    public void semAlerta(String titulo) {
        var i=interesses.buscar(dados.jogadorAtual().id(),ids.get(titulo)).orElseThrow();
        assertFalse(i.ativo());assertTrue(alertas.pendentePorInteresse(i.id()).isEmpty());
    }
    @Dado("que {int} jogadores marcaram interesse no lançamento de {string}")
    public void inscritos(int quantidade,String titulo) {
        if (atual==null) fixture(titulo,hoje().plusDays(1));
        interessados=quantidade;
        for (int n=0;n<quantidade;n++) {
            var u=dados.jogador("interessado-f12-"+n);
            var i=interesses.salvar(new InteresseEmLancamento(UUID.randomUUID(),u.id(),atual.jogoId(),true,Instant.now()));
            alertas.salvar(new NotificacaoLancamento(UUID.randomUUID(),i.id(),
                    DatasLancamentos.dispararEm(recarregar().dataPrevista()),false));
        }
    }
    @Dado("que faltam 24 horas para o lançamento de {string}")
    public void faltam24(String titulo) {
        instanteDaRotina=DatasLancamentos.dispararEm(recarregar().dataPrevista());
    }
    @Quando("a rotina de notificações é executada")
    public void disparar() { disparados=rotina.dispararAlertas(instanteDaRotina); }
    @Entao("{int} notificações de lançamento devem ser enviadas")
    public void enviadas(int quantidade) { assertEquals(quantidade,disparados); }
    @Quando("o administrador altera a data de {string} para daqui a {int} dias")
    public void alterar(String titulo,int dias) {
        contexto.obter(()->gestao.alterarData(atual.id(),hoje().plusDays(dias)));
    }
    @Entao("o histórico de alterações de {string} deve ter {int} registro")
    public void historico(String titulo,int quantidade) {
        assertEquals(quantidade,recarregar().historico().size());
    }
    @Entao("{int} jogadores devem ser avisados da nova data")
    public void avisados(int quantidade) {
        int total=0;
        for (int n=0;n<interessados;n++)
            total+=notificacoes.listarPorUsuario(dados.jogador("interessado-f12-"+n).id()).stream()
                    .filter(x->x.tipo()==TipoNotificacao.LANCAMENTO_ADIADO).count();
        assertEquals(quantidade,total);
    }
    @Quando("o usuário tenta alterar a data de {string}")
    public void comumAltera(String titulo) {
        fixture(titulo,hoje().plusDays(10));
        contexto.obter(()->gestao.alterarData(atual.id(),hoje().plusDays(20)));
    }
    @Dado("os lançamentos {string} em {int} dias, {string} em {int} dias e {string} em {int} dias")
    public void tresLancamentos(String a,int da,String b,int db,String c,int dc) {
        fixture(a,hoje().plusDays(da));fixture(b,hoje().plusDays(db));fixture(c,hoje().plusDays(dc));
    }
    @Dado("que o jogador marcou interesse em {string}")
    public void interesseNaLista(String titulo) {
        gestaoInteresse.marcar(lancamentos.buscarPorJogo(ids.get(titulo)).orElseThrow().id());
    }
    @Quando("o jogador consulta os lançamentos futuros")
    public void listar() { listados=consulta.listar(dados.jogadorAtual().id()); }
    @Entao("os lançamentos devem aparecer na ordem {string}, {string}, {string}")
    public void ordem(String a,String b,String c) {
        var presentes=listados.stream().filter(l->ids.containsValue(l.jogoId())).map(l->l.jogoId()).toList();
        assertEquals(List.of(ids.get(a),ids.get(b),ids.get(c)),presentes);
    }
    @Entao("apenas {string} deve estar destacado com alerta ativo")
    public void destaque(String titulo) {
        for (var l:listados) if (ids.containsValue(l.jogoId()))
            assertEquals(l.jogoId().equals(ids.get(titulo)),l.alertaAtivoDoUsuario());
    }

    @Entao("{string} deve mostrar {int} interessado na consulta")
    public void interessadosNaConsulta(String titulo, int quantidade) {
        var item=listados.stream().filter(l->l.jogoId().equals(ids.get(titulo))).findFirst().orElseThrow();
        assertEquals(quantidade,item.totalInteressados());
    }
    @Entao("o lançamento de {string} deve aparecer na consulta que inclui lançados")
    public void lancadoNaConsulta(String titulo) {
        assertTrue(consulta.listarComLancados(null).stream()
                .anyMatch(l->l.jogoId().equals(ids.get(titulo)) && l.status()==StatusLancamento.LANCADO));
    }

    @Dado("um desenvolvedor vinculado ao jogo {string} autenticado")
    public void desenvolvedorDoJogo(String titulo) {
        UUID desenvolvedoraId=jogos.buscarPorId(ids.get(titulo)).orElseThrow().desenvolvedoraId();
        var usuario=usuarios.salvar(dados.jogador("criador-f12").comPapel(Papel.DESENVOLVEDOR)
                .vincularDesenvolvedora(desenvolvedoraId));
        SessaoDeTeste.autenticarComo(usuario);
    }
    @Quando("o responsável altera o lançamento para daqui a {int} dias")
    public void responsavelAltera(int dias) { gestao.alterarData(atual.id(),hoje().plusDays(dias)); }
    @Entao("o jogo {string} deve ter a mesma data do lançamento")
    public void jogoSincronizado(String titulo) {
        assertEquals(recarregar().dataPrevista(),jogos.buscarPorId(ids.get(titulo)).orElseThrow().dataLancamento());
    }
    @Quando("o jogador marca interesse duas vezes em {string}")
    public void interesseRepetido(String titulo) {
        gestaoInteresse.marcar(atual.id());
        gestaoInteresse.marcar(atual.id());
    }
    @Entao("deve haver apenas um alerta pendente para {string}")
    public void apenasUmAlerta(String titulo) {
        var interesse=interesses.buscar(dados.jogadorAtual().id(),ids.get(titulo)).orElseThrow();
        assertTrue(alertas.pendentePorInteresse(interesse.id()).isPresent());
        assertEquals(1,alertas.vencidos(DatasLancamentos.dispararEm(recarregar().dataPrevista())).stream()
                .filter(a->a.interesseId().equals(interesse.id())).count());
    }
    @Quando("a rotina de notificações é executada duas vezes")
    public void dispararDuasVezes() {
        assertEquals(interessados,rotina.dispararAlertas(instanteDaRotina));
        assertEquals(0,rotina.dispararAlertas(instanteDaRotina));
    }
    @Entao("cada jogador recebe apenas um aviso de lançamento")
    public void avisosUnicos() {
        for (int n=0;n<interessados;n++) {
            var avisos=notificacoes.listarPorUsuario(dados.jogador("interessado-f12-"+n).id());
            assertEquals(1,avisos.stream().filter(a->a.tipo()==TipoNotificacao.LANCAMENTO_PROXIMO).count());
        }
    }
    @Quando("o catálogo altera a data de {string} para daqui a {int} dias")
    public void catalogoAdia(String titulo,int dias) { editarPeloCatalogo(titulo,hoje().plusDays(dias)); }
    @Quando("o catálogo altera a data de {string} para hoje")
    public void catalogoAntecipa(String titulo) { editarPeloCatalogo(titulo,hoje()); }
    private void editarPeloCatalogo(String titulo,LocalDate data) {
        Jogo jogo=jogos.buscarPorId(ids.get(titulo)).orElseThrow();
        atualizarCatalogo.executar(jogo.id(),jogo.titulo(),jogo.tituloOriginal(),jogo.resumo(),
                jogo.desenvolvedoraId(),jogo.generosIds(),data,jogo.capaUrl(),jogo.trailerUrl(),jogo.metadadosJson());
    }
    @Entao("os alertas dos interessados devem seguir a nova data")
    public void alertasReagendados() {
        for (int n=0;n<interessados;n++) {
            var interesse=interesses.buscar(dados.jogador("interessado-f12-"+n).id(),atual.jogoId()).orElseThrow();
            assertEquals(DatasLancamentos.dispararEm(recarregar().dataPrevista()),
                    alertas.pendentePorInteresse(interesse.id()).orElseThrow().dispararEm());
        }
    }
    @Dado("um jogo importado com lançamento inscrito para daqui a {int} dias")
    public void importadoInscrito(int dias) {
        Jogo base=dados.jogo("Importado F12");
        Jogo importado=Jogo.importar("Importado "+UUID.randomUUID(),"","","F12-"+UUID.randomUUID(),
                base.desenvolvedoraId(),Set.of(),hoje().plusDays(dias),"","");
        jogoImportadoId=jogos.salvar(importado).id();
        atual=gestao.cadastrar(jogoImportadoId,hoje().plusDays(dias));
    }
    @Quando("a importação atualiza a data do jogo para daqui a {int} dias")
    public void importacaoAdia(int dias) {
        Jogo jogo=jogos.buscarPorId(jogoImportadoId).orElseThrow();
        importarJogo.executar(jogo.titulo(),jogo.tituloOriginal(),jogo.resumo(),
                jogo.identificadorExterno(),jogo.desenvolvedoraId(),jogo.generosIds(),
                hoje().plusDays(dias),jogo.capaUrl(),jogo.trailerUrl());
    }
    @Entao("o histórico de alterações do jogo importado deve ter {int} registro")
    public void historicoImportado(int quantidade) { assertEquals(quantidade,recarregar().historico().size()); }
    @Entao("o jogo importado deve ter a mesma data do lançamento")
    public void importadoSincronizado() {
        assertEquals(recarregar().dataPrevista(),jogos.buscarPorId(jogoImportadoId).orElseThrow().dataLancamento());
    }
    @Entao("não deve haver alertas pendentes para os interessados")
    public void semAlertasDosInteressados() {
        for (int n=0;n<interessados;n++) {
            var interesse=interesses.buscar(dados.jogador("interessado-f12-"+n).id(),atual.jogoId());
            interesse.ifPresent(i->assertTrue(alertas.pendentePorInteresse(i.id()).isEmpty()));
        }
    }
    @Quando("o administrador exclui o lançamento de {string}")
    public void excluirLancamento(String titulo) { gestao.excluir(atual.id()); }
    @Entao("o jogo {string} continua no catálogo")
    public void jogoPermanece(String titulo) {
        assertTrue(jogos.buscarPorId(ids.get(titulo)).isPresent());
        assertTrue(lancamentos.buscar(atual.id()).isEmpty());
    }
    @Quando("o catálogo desativa o jogo {string}")
    public void desativarJogo(String titulo) { desativarJogo.executar(ids.get(titulo)); }
    @Entao("o lançamento de {string} não deve aparecer na agenda pública")
    public void naoApareceNaAgenda(String titulo) {
        assertTrue(consulta.listar(null).stream().noneMatch(l->l.jogoId().equals(ids.get(titulo))));
    }
    @Quando("o administrador cadastra uma data estimada para {string} daqui a {int} dias")
    public void cadastrarEstimativa(String titulo,int dias) {
        UUID jogoId=dados.jogo(titulo).id(); ids.put(titulo,jogoId);
        assertNull(jogos.buscarPorId(jogoId).orElseThrow().dataLancamento());
        atual=gestao.cadastrar(jogoId,hoje().plusDays(dias),TipoDataLancamento.ESTIMADA);
    }
    @Dado("que o lançamento de {string} tem data estimada para daqui a {int} dias")
    public void estimadoEmDias(String titulo,int dias) {
        fixture(titulo,hoje().plusDays(dias));
        atual=lancamentos.salvar(recarregar().comTipoData(TipoDataLancamento.ESTIMADA,hoje()));
    }
    @Dado("que o lançamento de {string} tem data estimada para ontem")
    public void estimadoOntem(String titulo) {
        fixture(titulo,hoje().minusDays(1));
        atual=lancamentos.salvar(recarregar().comTipoData(TipoDataLancamento.ESTIMADA,hoje()));
    }
    @Entao("{string} aparece na agenda pública com data estimada")
    public void visivelComoEstimativa(String titulo) {
        assertTrue(consulta.listar(null).stream().anyMatch(l->l.jogoId().equals(ids.get(titulo))
                && l.tipoData()==TipoDataLancamento.ESTIMADA));
    }
    @Entao("o interesse em {string} não deve ter alerta pendente")
    public void interessadoSemAlerta(String titulo) {
        var interesse=interesses.buscar(dados.jogadorAtual().id(),ids.get(titulo)).orElseThrow();
        assertTrue(interesse.ativo());
        assertTrue(alertas.pendentePorInteresse(interesse.id()).isEmpty());
    }
    @Dado("que {int} jogadores marcaram interesse sem alerta em {string}")
    public void interessadosEmEstimativa(int quantidade,String titulo) {
        interessados=quantidade;
        for (int n=0;n<quantidade;n++) {
            var usuario=dados.jogador("interessado-f12-"+n);
            interesses.salvar(new InteresseEmLancamento(UUID.randomUUID(),usuario.id(),atual.jogoId(),true,Instant.now()));
        }
    }
    @Quando("o administrador confirma a data de {string}")
    public void confirmar(String titulo) {
        gestao.alterarData(atual.id(),recarregar().dataPrevista(),TipoDataLancamento.CONFIRMADA);
        assertEquals(TipoDataLancamento.CONFIRMADA,recarregar().tipoData());
    }
    @Entao("o lançamento de {string} mantém a data estimada")
    public void mantemEstimativa(String titulo) {
        assertEquals(TipoDataLancamento.ESTIMADA,recarregar().tipoData());
    }

    @Quando("o administrador reclassifica a data de {string} como estimada")
    public void reclassificarComoEstimativa(String titulo) {
        gestao.alterarData(atual.id(),recarregar().dataPrevista(),TipoDataLancamento.ESTIMADA);
    }

}
