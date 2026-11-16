package com.g4.game_tracker_backend.aplicacao.descoberta.similaridade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.PerfilJogoGateway;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeJogo;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeRepositorio;

/**
 * F-10 RN 3 (OverrideSimilaridadeService do modelo): só o ADMINISTRADOR cria uma conexão manual entre dois
 * jogos ATIVOS, com o grau que quiser, ou remove uma conexão. Os demais recebem "Apenas administradores
 * podem ajustar similaridades".
 *
 * A conexão manual substitui a automática do mesmo par (e reativa uma removida). A remoção deixa a conexão
 * REMOVIDA, para o recálculo automático não a trazer de volta.
 */
@Service
public class AjustarSimilaridadeManual {

    private final SimilaridadeRepositorio similaridades;
    private final PerfilJogoGateway perfis;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public AjustarSimilaridadeManual(SimilaridadeRepositorio similaridades, PerfilJogoGateway perfis,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.similaridades = similaridades;
        this.perfis = perfis;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    @Transactional
    public SimilaridadeJogo criarManual(UUID jogoUm, UUID jogoDois, BigDecimal grau) {
        Sugestoes.exigirAdministrador(verificadorDeAcesso);
        if (jogoUm == null || jogoDois == null) {
            throw new RegraDeNegocioException("Informe os dois jogos da conexão");
        }
        Instant agora = Instant.now();
        SimilaridadeJogo conexao = similaridades.buscarPar(jogoUm, jogoDois)
                .map(existente -> existente.definirManual(grau, agora))
                .orElseGet(() -> SimilaridadeJogo.manual(jogoUm, jogoDois, grau, agora));
        if (perfis.obterPerfil(jogoUm).isEmpty() || perfis.obterPerfil(jogoDois).isEmpty()) {
            throw new RegraDeNegocioException("Os dois jogos precisam estar ativos no catálogo");
        }
        return similaridades.salvar(conexao);
    }

    @Transactional
    public void removerConexao(UUID similaridadeId) {
        Sugestoes.exigirAdministrador(verificadorDeAcesso);
        SimilaridadeJogo conexao = Sugestoes.visivel(similaridades, similaridadeId);
        similaridades.salvar(conexao.remover(Instant.now()));
    }
}
