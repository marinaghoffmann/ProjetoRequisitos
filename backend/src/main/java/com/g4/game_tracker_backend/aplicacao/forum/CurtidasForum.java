package com.g4.game_tracker_backend.aplicacao.forum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.g4.game_tracker_backend.aplicacao.identidade.VerificadorDeAcesso;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForumRepositorio;
import com.g4.game_tracker_backend.dominio.forum.CurtidaForum;
import com.g4.game_tracker_backend.dominio.forum.CurtidaForumRepositorio;
import com.g4.game_tracker_backend.aplicacao.moderacao.EstadoPublico;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;
import com.g4.game_tracker_backend.aplicacao.compartilhado.CasoDeUso;
import com.g4.game_tracker_backend.aplicacao.compartilhado.Transacional;

/** Curtidas idempotentes. O bloqueio da raiz serializa escritas e mantém a contagem em alta correta. */
@CasoDeUso
public class CurtidasForum {
    private final TopicoDeForumRepositorio topicos;
    private final CurtidaForumRepositorio curtidas;
    private final VerificadorDeAcesso acesso;
    private final EstadoPublico moderacao;

    public CurtidasForum(TopicoDeForumRepositorio topicos, CurtidaForumRepositorio curtidas, VerificadorDeAcesso acesso,
            EstadoPublico moderacao) {
        this.topicos = topicos;
        this.curtidas = curtidas;
        this.acesso = acesso;
        this.moderacao = moderacao;
    }

    @Transacional
    public void definir(UUID topicoId, UUID respostaId, boolean curtir) {
        TopicoDeForum topico = topicos.buscarPorIdParaAlteracao(topicoId)
                .filter(t -> !t.removido())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tópico não encontrado"));
        if (respostaId != null && topico.resposta(respostaId).removida()) {
            throw new RecursoNaoEncontradoException("Resposta removida");
        }
        moderacao.exigirAtivo(TipoConteudo.TOPICO,topicoId);
        if (respostaId != null) moderacao.exigirAtivo(TipoConteudo.RESPOSTA,respostaId);
        UUID usuarioId = acesso.usuarioAtualId();
        String tipo = respostaId == null ? "TOPICO" : "RESPOSTA";
        UUID conteudoId = respostaId == null ? topicoId : respostaId;
        boolean existente = curtidas.existe(usuarioId, tipo, conteudoId);
        if (curtir && !existente) {
            curtidas.adicionar(new CurtidaForum(usuarioId, topicoId, tipo, conteudoId));
        } else if (!curtir && existente) {
            curtidas.remover(usuarioId, tipo, conteudoId);
        } else {
            return;
        }
        curtidas.sincronizarTotal(topicoId);
    }

    @Transacional(somenteLeitura = true)
    public Resumo resumo(UUID topicoId, UUID usuarioId) {
        List<CurtidaForum> itens = curtidas.listar(topicoId);
        Map<UUID, Integer> porResposta = new HashMap<>();
        Set<UUID> minhas = new HashSet<>();
        int totalTopico = 0;
        for (CurtidaForum curtida : itens) {
            TipoConteudo tipoModerado = curtida.tipo().equals("TOPICO")
                    ? TipoConteudo.TOPICO
                    : TipoConteudo.RESPOSTA;
            if (moderacao.oculto(tipoModerado,curtida.conteudoId()) || moderacao.removido(tipoModerado,curtida.conteudoId())) continue;
            if (curtida.tipo().equals("TOPICO")) totalTopico++;
            else porResposta.merge(curtida.conteudoId(), 1, Integer::sum);
            if (usuarioId != null && usuarioId.equals(curtida.usuarioId())) minhas.add(curtida.conteudoId());
        }
        return new Resumo(totalTopico, Map.copyOf(porResposta), Set.copyOf(minhas),
                totalTopico + porResposta.values().stream().mapToInt(Integer::intValue).sum());
    }

    public record Resumo(int totalTopico, Map<UUID, Integer> porResposta,
            Set<UUID> curtidasDoUsuario, int total) {}
}
