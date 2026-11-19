package com.g4.game_tracker_backend.aplicacao.lancamentos;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.g4.game_tracker_backend.dominio.catalogo.*;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.lancamentos.*;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;
@CasoDeUso
public class ConsultarLancamentos {
    public record Visao(UUID id, UUID jogoId, UUID desenvolvedoraId, String titulo, String capaUrl, LocalDate dataPrevista,
            StatusLancamento status, TipoDataLancamento tipoData, long diasRestantes, long totalInteressados, boolean alertaAtivoDoUsuario,
            List<AlteracaoCronograma> historico) {}
    private final LancamentoRepositorio lancamentos;
    private final InteresseLancamentoRepositorio interesses;
    private final JogoRepositorio jogos;
    public ConsultarLancamentos(LancamentoRepositorio lancamentos, InteresseLancamentoRepositorio interesses,
            JogoRepositorio jogos) {
        this.lancamentos=lancamentos;this.interesses=interesses;this.jogos=jogos;
    }
    @Transacional(somenteLeitura = true)
    public List<Visao> listar(UUID usuarioId) {
        Instant agora=Instant.now();
        return lancamentos.listarFuturos().stream()
                .filter(l->l.tipoData()==TipoDataLancamento.ESTIMADA
                        || l.dataPrevista().isAfter(DatasLancamentos.hoje(agora)))
                .filter(l->jogos.buscarPorId(l.jogoId())
                .map(j->j.status()==StatusCatalogo.ATIVO).orElse(false))
                .map(l->visao(l,usuarioId,agora)).toList();
    }
    @Transacional(somenteLeitura = true)
    public List<Visao> listarComLancados(UUID usuarioId) {
        Instant agora=Instant.now();
        List<Visao> resultado=new java.util.ArrayList<>(listar(usuarioId));
        lancamentos.listarLancados().stream()
                .filter(l->jogos.buscarPorId(l.jogoId())
                        .map(j->j.status()==StatusCatalogo.ATIVO).orElse(false))
                .map(l->visao(l,usuarioId,agora)).forEach(resultado::add);
        return List.copyOf(resultado);
    }
    @Transacional(somenteLeitura = true)
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
                DatasLancamentos.diasRestantes(l.dataPrevista(),agora),
                interesses.contarAtivosPorJogo(l.jogoId()),ativo,l.historico());
    }
}
