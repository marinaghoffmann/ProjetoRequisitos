package com.g4.game_tracker_backend.aplicacao.lancamentos;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.lancamentos.*;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;
@CasoDeUso
public class GerenciarInteresseLancamento {
    private final LancamentoRepositorio lancamentos;
    private final InteresseLancamentoRepositorio interesses;
    private final AlertaLancamentoRepositorio alertas;
    private final VerificadorDeAcesso acesso;
    private final JogoRepositorio jogos;
    public GerenciarInteresseLancamento(LancamentoRepositorio lancamentos,InteresseLancamentoRepositorio interesses,
            AlertaLancamentoRepositorio alertas,VerificadorDeAcesso acesso,JogoRepositorio jogos) {
        this.lancamentos=lancamentos;this.interesses=interesses;this.alertas=alertas;this.acesso=acesso;this.jogos=jogos;
    }
    @Transacional
    public void marcar(UUID lancamentoId) {
        LancamentoFuturo l=futuro(lancamentoId);
        UUID usuario=acesso.usuarioAtualId();
        Instant agora=Instant.now();
        var existente=interesses.buscar(usuario,l.jogoId());
        if (existente.isPresent() && existente.get().ativo()) return;
        InteresseEmLancamento i=existente
                .map(atual->atual.comAtivo(true,agora))
                .orElseGet(()->new InteresseEmLancamento(UUID.randomUUID(),usuario,l.jogoId(),true,agora));
        interesses.salvar(i);
        if (l.tipoData()==TipoDataLancamento.CONFIRMADA && alertas.pendentePorInteresse(i.id()).isEmpty())
            agendar(i,l.dataPrevista(),agora);
    }
    @Transacional
    public void desmarcar(UUID lancamentoId) {
        LancamentoFuturo l=lancamentos.buscar(lancamentoId)
                .orElseThrow(()->new RecursoNaoEncontradoException("Lançamento não encontrado"));
        interesses.buscar(acesso.usuarioAtualId(),l.jogoId()).ifPresent(i->{
            interesses.salvar(i.comAtivo(false,Instant.now()));
            alertas.cancelarPendentes(i.id());
        });
    }
    @Transacional
    public void reagendar(UUID jogoId,LocalDate novaData,Instant agora) {
        boolean confirmada=lancamentos.buscarPorJogo(jogoId)
                .map(l->l.tipoData()==TipoDataLancamento.CONFIRMADA).orElse(false);
        for (InteresseEmLancamento i: interesses.ativosPorJogo(jogoId)) {
            alertas.cancelarPendentes(i.id());
            if (confirmada && novaData.isAfter(DatasLancamentos.hoje(agora))) agendar(i,novaData,agora);
        }
    }
    @Transacional
    public void cancelarTodos(UUID jogoId) {
        for (InteresseEmLancamento i: interesses.todosPorJogo(jogoId)) alertas.cancelarPendentes(i.id());
    }
    private void agendar(InteresseEmLancamento i,LocalDate data,Instant agora) {
        Instant disparar=DatasLancamentos.dispararEm(data);
        alertas.salvar(new NotificacaoLancamento(UUID.randomUUID(),i.id(),
                disparar.isBefore(agora)?agora:disparar,false));
    }
    private LancamentoFuturo futuro(UUID id) {
        LancamentoFuturo l=lancamentos.buscar(id)
                .orElseThrow(()->new RecursoNaoEncontradoException("Lançamento não encontrado"));
        if (jogos.buscarPorId(l.jogoId()).map(j->j.status()!=StatusCatalogo.ATIVO).orElse(true))
            throw new RegraDeNegocioException("O jogo não está ativo no catálogo");
        if (l.status()!=StatusLancamento.FUTURO_LANCAMENTO
                || (l.tipoData()==TipoDataLancamento.CONFIRMADA
                        && !l.dataPrevista().isAfter(DatasLancamentos.hoje(Instant.now()))))
            throw new RegraDeNegocioException("Este jogo já foi lançado");
        return l;
    }
}
