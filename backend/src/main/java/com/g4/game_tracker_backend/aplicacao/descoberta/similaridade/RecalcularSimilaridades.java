package com.g4.game_tracker_backend.aplicacao.descoberta.similaridade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.CalculoSimilaridade;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.PerfilJogo;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.PerfilJogoGateway;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeJogo;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeRepositorio;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

/**
 * F-10 (CalculoSimilaridadeService do modelo): compara o jogo com os demais jogos ATIVOS do catálogo,
 * usando o CalculoSimilaridade (Strategy), e grava os pares com grau acima do mínimo (0,20).
 *
 * RN 1: o par é um só para os dois sentidos. RN 3: conexões MANUAIS ou REMOVIDAS pelo administrador não são
 * alteradas nem recriadas. Pares automáticos que ficam abaixo do mínimo deixam de aparecer.
 *
 * Não está ligado ao cadastro de jogos da F-01: roda pelo endpoint de administrador ou sob demanda, na
 * primeira consulta de um jogo que ainda não tem similares calculados.
 */
@CasoDeUso
public class RecalcularSimilaridades {

    private final CalculoSimilaridade calculo = CalculoSimilaridade.padrao();
    private final PerfilJogoGateway perfis;
    private final SimilaridadeRepositorio similaridades;
    private final VerificadorDeAcesso verificadorDeAcesso;

    public RecalcularSimilaridades(PerfilJogoGateway perfis, SimilaridadeRepositorio similaridades,
            VerificadorDeAcesso verificadorDeAcesso) {
        this.perfis = perfis;
        this.similaridades = similaridades;
        this.verificadorDeAcesso = verificadorDeAcesso;
    }

    /** Recálculo pedido pelo administrador (POST .../similares/recalcular). */
    @Transacional
    public List<SimilaridadeJogo> executarComoAdministrador(UUID jogoId) {
        Sugestoes.exigirAdministrador(verificadorDeAcesso);
        return recalcular(jogoId);
    }

    /** Recálculo do sistema: usado pela consulta sob demanda. Devolve as conexões visíveis do jogo. */
    @Transacional
    public List<SimilaridadeJogo> recalcular(UUID jogoId) {
        PerfilJogo perfil = perfis.obterPerfil(jogoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
        Instant agora = Instant.now();
        Map<UUID, SimilaridadeJogo> existentes = new HashMap<>();
        similaridades.listarDoJogo(jogoId).forEach(conexao -> existentes.put(conexao.outroJogo(jogoId), conexao));

        for (PerfilJogo candidato : perfis.listarCandidatos(jogoId)) {
            BigDecimal grau = calculo.calcular(perfil, candidato);
            SimilaridadeJogo existente = existentes.get(candidato.jogoId());
            if (existente != null) {
                if (existente.aceitaRecalculo() && existente.grauBase().compareTo(grau) != 0) {
                    similaridades.salvar(existente.recalcular(grau, agora));
                }
            } else if (SimilaridadeJogo.acimaDoMinimo(grau)) {
                similaridades.salvar(SimilaridadeJogo.automatica(jogoId, candidato.jogoId(), grau, agora));
            }
        }
        return similaridades.listarDoJogo(jogoId).stream().filter(SimilaridadeJogo::visivel).toList();
    }
}
