package com.g4.game_tracker_backend.infraestrutura.persistencia.forum;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.forum.CurtidaForum;
import com.g4.game_tracker_backend.dominio.forum.CurtidaForumRepositorio;

@Repository
public class CurtidaForumRepositorioPostgres implements CurtidaForumRepositorio {
    private final CurtidaForumSpringDataRepository curtidas;
    private final TopicoForumSpringDataRepository topicos;

    public CurtidaForumRepositorioPostgres(CurtidaForumSpringDataRepository curtidas,
            TopicoForumSpringDataRepository topicos) {
        this.curtidas = curtidas;
        this.topicos = topicos;
    }

    @Override
    public boolean existe(UUID usuarioId, String tipo, UUID conteudoId) {
        return curtidas.findByUsuarioIdAndTipoAndConteudoId(usuarioId, tipo, conteudoId).isPresent();
    }

    @Override
    public void adicionar(CurtidaForum curtida) {
        curtidas.saveAndFlush(new CurtidaForumJpa(curtida.usuarioId(), curtida.topicoId(),
                curtida.tipo(), curtida.conteudoId()));
    }

    @Override
    public void remover(UUID usuarioId, String tipo, UUID conteudoId) {
        curtidas.findByUsuarioIdAndTipoAndConteudoId(usuarioId, tipo, conteudoId).ifPresent(curtidas::delete);
        curtidas.flush();
    }

    @Override
    public List<CurtidaForum> listar(UUID topicoId) {
        return curtidas.findAllByTopicoId(topicoId).stream()
                .map(item -> new CurtidaForum(item.usuarioId(), topicoId, item.tipo(), item.conteudoId()))
                .toList();
    }

    @Override
    public void removerDaResposta(UUID respostaId) {
        curtidas.deleteByTipoAndConteudoId("RESPOSTA", respostaId);
        curtidas.flush();
    }

    @Override
    public void sincronizarTotal(UUID topicoId) {
        topicos.atualizarCurtidas(topicoId, Math.toIntExact(curtidas.countByTopicoId(topicoId)));
    }
}
