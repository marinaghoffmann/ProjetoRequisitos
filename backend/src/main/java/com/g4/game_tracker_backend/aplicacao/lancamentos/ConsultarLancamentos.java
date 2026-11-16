package com.g4.game_tracker_backend.aplicacao.lancamentos;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.g4.game_tracker_backend.dominio.catalogo.*;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.lancamentos.*;
@Service
public class ConsultarLancamentos {
    public record Visao(UUID id, UUID jogoId, UUID desenvolvedoraId, String titulo, String capaUrl, LocalDate dataPrevista,
            StatusLancamento status, TipoDataLancamento tipoData, long diasRestantes, boolean alertaAtivoDoUsuario,
            List<AlteracaoCronograma> historico) {}
    private final LancamentoRepositorio lancamentos;
    private final InteresseLancamentoRepositorio interesses;
    private final JogoRepositorio jogos;
    public ConsultarLancamentos(LancamentoRepositorio lancamentos, InteresseLancamentoRepositorio interesses,
            JogoRepositorio jogos) {
        this.lancamentos=lancamentos;this.interesses=interesses;this.jogos=jogos;
    }
    @Transactional(readOnly=true)
    public List<Visao> listar(UUID usuarioId) {
        Instant agora=Instant.now();
        return lancamentos.listarFuturos().stream()
                .filter(l->l.tipoData()==TipoDataLancamento.ESTIMADA
                        || l.dataPrevista().isAfter(DatasLancamentos.hoje(agora)))
                .filter(l->jogos.buscarPorId(l.jogoId())
                .map(j->j.status()==StatusCatalogo.ATIVO).orElse(false))
                .map(l->visao(l,usuarioId,agora)).toList();
    }
    @Transactional(readOnly=true)
    public Visao buscar(UUID id,UUID usuarioId) {
        LancamentoFuturo l=lancamentos.buscar(id)
                .orElseThrow(()->new RecursoNaoEncontradoException("Lançamento não encontrado"));
        return visao(l,usuarioId,Instant.now());
    }
    private Visao visao(LancamentoFuturo l,UUID usuarioId,Instant agora) {
        Jogo j=jogos.buscarPorId(l.jogoId())
                .orElseThrow(()->new RecursoNaoEncontradoException("Jogo não encontrado"));
        boolean ativo=usuarioId!=null && interesses.buscar(usuarioId,l.jogoId())
                .map(InteresseEmLancamento::ativo).orElse(false);
        return new Visao(l.id(),l.jogoId(),j.desenvolvedoraId(),j.titulo(),j.capaUrl(),l.dataPrevista(),l.status(),l.tipoData(),
                DatasLancamentos.diasRestantes(l.dataPrevista(),agora),ativo,l.historico());
    }
}
