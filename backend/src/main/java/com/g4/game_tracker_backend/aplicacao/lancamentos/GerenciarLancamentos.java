package com.g4.game_tracker_backend.aplicacao.lancamentos;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.g4.game_tracker_backend.aplicacao.catalogo.SalvarJogoComEvento;
import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.catalogo.*;
import com.g4.game_tracker_backend.dominio.compartilhado.*;
import com.g4.game_tracker_backend.dominio.identidade.*;
import com.g4.game_tracker_backend.dominio.lancamentos.*;
@Service
public class GerenciarLancamentos {
    private final LancamentoRepositorio lancamentos;
    private final InteresseLancamentoRepositorio interesses;
    private final AlertaLancamentoRepositorio alertas;
    private final JogoRepositorio jogos;
    private final UsuarioRepositorio usuarios;
    private final VerificadorDeAcesso acesso;
    private final SalvarJogoComEvento salvarJogo;
    private final GerenciarInteresseLancamento gestaoInteresses;
    public GerenciarLancamentos(LancamentoRepositorio lancamentos,InteresseLancamentoRepositorio interesses,
            AlertaLancamentoRepositorio alertas,JogoRepositorio jogos,UsuarioRepositorio usuarios,
            VerificadorDeAcesso acesso,SalvarJogoComEvento salvarJogo,
            GerenciarInteresseLancamento gestaoInteresses) {
        this.lancamentos=lancamentos;this.interesses=interesses;this.alertas=alertas;this.jogos=jogos;
        this.usuarios=usuarios;this.acesso=acesso;this.salvarJogo=salvarJogo;
        this.gestaoInteresses=gestaoInteresses;
    }
    @Transactional
    public LancamentoFuturo cadastrar(UUID jogoId,LocalDate data) {
        return cadastrar(jogoId,data,TipoDataLancamento.CONFIRMADA);
    }
    @Transactional
    public LancamentoFuturo cadastrar(UUID jogoId,LocalDate data,TipoDataLancamento tipoData) {
        Jogo jogo=jogoAutorizado(jogoId);
        Instant agora=Instant.now();
        LancamentoFuturo.exigirFutura(data,DatasLancamentos.hoje(agora));
        if (lancamentos.buscarPorJogo(jogoId).isPresent())
            throw new RegraDeNegocioException("Este jogo já possui um lançamento cadastrado");
        LancamentoFuturo l=lancamentos.salvar(LancamentoFuturo.criar(jogoId,data,DatasLancamentos.hoje(agora),agora,
                tipoData == null ? TipoDataLancamento.CONFIRMADA : tipoData));
        salvarJogo.atualizarData(jogo.id(),data);
        return l;
    }
    @Transactional
    public LancamentoFuturo alterarData(UUID id,LocalDate data) {
        return alterarData(id,data,null);
    }
    @Transactional
    public LancamentoFuturo alterarData(UUID id,LocalDate data,TipoDataLancamento tipoData) {
        LancamentoFuturo l=buscar(id);
        jogoAutorizado(l.jogoId());
        Instant agora=Instant.now();
        LancamentoFuturo.exigirFutura(data,DatasLancamentos.hoje(agora));
        TipoDataLancamento escolhido=tipoData == null ? l.tipoData() : tipoData;
        if (escolhido != l.tipoData()) {
            lancamentos.salvar(l.comTipoData(escolhido,DatasLancamentos.hoje(agora)));
            if (l.dataPrevista().equals(data))
                gestaoInteresses.reagendar(l.jogoId(),data,agora);
        }
        if (!l.dataPrevista().equals(data)) salvarJogo.atualizarData(l.jogoId(),data);
        return buscar(id);
    }
    @Transactional
    public void excluir(UUID id) {
        LancamentoFuturo l=buscar(id);
        jogoAutorizado(l.jogoId());
        for (InteresseEmLancamento i: interesses.todosPorJogo(l.jogoId())) alertas.excluirPorInteresse(i.id());
        interesses.excluirPorJogo(l.jogoId());
        lancamentos.excluir(id);
    }
    private LancamentoFuturo buscar(UUID id) {
        return lancamentos.buscar(id).orElseThrow(()->new RecursoNaoEncontradoException("Lançamento não encontrado"));
    }
    private Jogo jogoAutorizado(UUID id) {
        Jogo j=jogos.buscarPorId(id).filter(x->x.status()==StatusCatalogo.ATIVO)
                .orElseThrow(()->new RecursoNaoEncontradoException("Jogo ativo não encontrado"));
        if (acesso.usuarioAtualTemAlgumPapel(Papel.ADMINISTRADOR)) return j;
        if (acesso.usuarioAtualTemAlgumPapel(Papel.DESENVOLVEDOR)
                && usuarios.buscarPorId(acesso.usuarioAtualId())
                        .map(Usuario::desenvolvedoraId).filter(j.desenvolvedoraId()::equals).isPresent()) return j;
        throw new AcessoNegadoException("Apenas administradores ou criadores podem alterar lançamentos");
    }
}
