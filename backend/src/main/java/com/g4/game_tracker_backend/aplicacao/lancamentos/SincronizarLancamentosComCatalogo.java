package com.g4.game_tracker_backend.aplicacao.lancamentos;
import java.time.Instant;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.g4.game_tracker_backend.aplicacao.notificacao.DespacharNotificacao;
import com.g4.game_tracker_backend.dominio.catalogo.DataJogoAlterada;
import com.g4.game_tracker_backend.dominio.catalogo.JogoDesativado;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.lancamentos.*;
import com.g4.game_tracker_backend.dominio.notificacao.TipoNotificacao;
@Component
public class SincronizarLancamentosComCatalogo {
    private final LancamentoRepositorio lancamentos;
    private final InteresseLancamentoRepositorio interesses;
    private final GerenciarInteresseLancamento gestaoInteresses;
    private final DespacharNotificacao notificacoes;
    private final AlertaLancamentoRepositorio alertas;
    public SincronizarLancamentosComCatalogo(LancamentoRepositorio lancamentos,
            InteresseLancamentoRepositorio interesses,GerenciarInteresseLancamento gestaoInteresses,
            DespacharNotificacao notificacoes, AlertaLancamentoRepositorio alertas) {
        this.lancamentos=lancamentos;this.interesses=interesses;this.gestaoInteresses=gestaoInteresses;
        this.notificacoes=notificacoes;this.alertas=alertas;
    }

    /** Jogo desativado no catálogo (F-01 RN 3): os alertas pendentes dos interessados são cancelados. */
    @EventListener
    @Transactional(propagation=Propagation.MANDATORY)
    public void jogoDesativado(JogoDesativado evento) {
        for (InteresseEmLancamento interesse : interesses.ativosPorJogo(evento.jogoId())) {
            alertas.cancelarPendentes(interesse.id());
        }
    }
    @EventListener
    @Transactional(propagation=Propagation.MANDATORY)
    public void dataAlterada(DataJogoAlterada evento) {
        lancamentos.buscarPorJogo(evento.jogoId()).ifPresent(atual->{
            if (evento.nova()==null) throw new RegraDeNegocioException(
                    "Um lançamento cadastrado precisa manter a data prevista");
            if (atual.dataPrevista().equals(evento.nova())) return;
            Instant agora=Instant.now();
            LancamentoFuturo atualizado=atual.alterarData(evento.nova(),evento.alteradoPorId(),
                    DatasLancamentos.hoje(agora),agora);
            lancamentos.salvar(atualizado);
            gestaoInteresses.reagendar(atual.jogoId(),evento.nova(),agora);
            for (InteresseEmLancamento i: interesses.ativosPorJogo(atual.jogoId())) {
                notificacoes.enviar(i.usuarioId(),TipoNotificacao.LANCAMENTO_ADIADO,
                        "Data de lançamento alterada",
                        "A data de lançamento mudou de "+atual.dataPrevista()+" para "+evento.nova()+".",
                        "data:"+atualizado.historico().getLast().id()+":"+i.usuarioId(),agora);
            }
        });
    }
}
