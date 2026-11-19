package com.g4.game_tracker_backend.aplicacao.lancamentos;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.g4.game_tracker_backend.aplicacao.notificacao.DespacharNotificacao;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;
import com.g4.game_tracker_backend.dominio.lancamentos.*;
import com.g4.game_tracker_backend.dominio.notificacao.TipoNotificacao;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.ExecutorDeTransacao;
@CasoDeUso
public class ProcessarLancamentos {
    private static final Logger LOG=LoggerFactory.getLogger(ProcessarLancamentos.class);
    private final LancamentoRepositorio lancamentos;
    private final InteresseLancamentoRepositorio interesses;
    private final AlertaLancamentoRepositorio alertas;
    private final JogoRepositorio jogos;
    private final DespacharNotificacao notificacoes;
    private final ExecutorDeTransacao transacao;
    public ProcessarLancamentos(LancamentoRepositorio lancamentos,InteresseLancamentoRepositorio interesses,
            AlertaLancamentoRepositorio alertas,JogoRepositorio jogos,DespacharNotificacao notificacoes,
            ExecutorDeTransacao transacao) {
        this.lancamentos=lancamentos;this.interesses=interesses;this.alertas=alertas;
        this.jogos=jogos;this.notificacoes=notificacoes;this.transacao=transacao;
    }
    public int transicionar(Instant agora) {
        int total=0;
        for (LancamentoFuturo l:lancamentos.listarVencidos(DatasLancamentos.hoje(agora))) {
            if (l.tipoData()==TipoDataLancamento.ESTIMADA) continue;
            try {
                transacao.executarSemRetorno(()->{
                    lancamentos.buscar(l.id()).filter(a->a.status()==StatusLancamento.FUTURO_LANCAMENTO
                            && a.tipoData()==TipoDataLancamento.CONFIRMADA)
                            .ifPresent(a->lancamentos.salvar(a.lancar()));
                    for (InteresseEmLancamento i:interesses.ativosPorJogo(l.jogoId()))
                        alertas.cancelarPendentes(i.id());
                });
                total++;
            } catch (RuntimeException e) { LOG.error("Falha na transição do lançamento {}",l.id(),e); }
        }
        return total;
    }
    public int dispararAlertas(Instant agora) {
        int total=0;
        for (NotificacaoLancamento candidato:alertas.vencidos(agora)) {
            try {
                Boolean enviada=transacao.executar(()->{
                    NotificacaoLancamento a=alertas.buscar(candidato.id()).orElse(null);
                    if (a==null || a.enviada()) return false;
                    InteresseEmLancamento i=interesses.buscarPorId(a.interesseId()).orElse(null);
                    if (i==null || !i.ativo()) { alertas.cancelarPendentes(a.interesseId()); return false; }
                    LancamentoFuturo l=lancamentos.buscarPorJogo(i.jogoId()).orElse(null);
                    if (l==null || l.tipoData()!=TipoDataLancamento.CONFIRMADA
                            || l.status()!=StatusLancamento.FUTURO_LANCAMENTO
                            || !l.dataPrevista().isAfter(DatasLancamentos.hoje(agora))) {
                        alertas.cancelarPendentes(i.id()); return false;
                    }
                    var jogo=jogos.buscarPorId(i.jogoId()).orElse(null);
                    if (jogo==null || jogo.status()!=StatusCatalogo.ATIVO) {
                        alertas.cancelarPendentes(i.id()); return false;
                    }
                    String titulo=jogo.titulo();
                    notificacoes.enviar(i.usuarioId(),TipoNotificacao.LANCAMENTO_PROXIMO,
                            "Lançamento próximo",titulo+" será lançado em "+l.dataPrevista()+".",
                            "alerta:"+a.id(),agora);
                    alertas.salvar(a.marcarEnviada());
                    return true;
                });
                if (Boolean.TRUE.equals(enviada)) total++;
            } catch (RuntimeException e) { LOG.error("Falha no alerta {}",candidato.id(),e); }
        }
        return total;
    }
}
