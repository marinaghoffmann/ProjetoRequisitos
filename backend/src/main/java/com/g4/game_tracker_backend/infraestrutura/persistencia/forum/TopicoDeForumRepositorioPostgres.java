package com.g4.game_tracker_backend.infraestrutura.persistencia.forum;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import com.g4.game_tracker_backend.dominio.forum.FiltroTopicos;
import com.g4.game_tracker_backend.dominio.forum.ResumoTopico;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForumRepositorio;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

@Repository
public class TopicoDeForumRepositorioPostgres implements TopicoDeForumRepositorio {

    private final TopicoForumSpringDataRepository springData;
    private final com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao.EstadoConteudoRepository estados;

    public TopicoDeForumRepositorioPostgres(TopicoForumSpringDataRepository springData,
            com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao.EstadoConteudoRepository estados) {
        this.springData = springData;
        this.estados = estados;
    }

    @Override
    public TopicoDeForum salvar(TopicoDeForum topico) {
        int curtidas = springData.findById(topico.id()).map(TopicoForumJpa::totalCurtidas).orElse(0);
        java.util.List<UUID> ativas = topico.respostas().stream().filter(r -> !r.removida()).map(r -> r.id()).toList();
        int removidasPelaModeracao = ativas.isEmpty() ? 0 : Math.toIntExact(estados.countByTipoAndConteudoIdInAndOcultoTrueOrTipoAndConteudoIdInAndRemovidoTrue(
                TipoConteudo.RESPOSTA, ativas,
                TipoConteudo.RESPOSTA, ativas));
        springData.save(TopicoForumJpa.deDominio(topico, curtidas, topico.totalRespostas() - removidasPelaModeracao));
        return topico;
    }

    @Override
    public Optional<TopicoDeForum> buscarPorId(UUID id) {
        return springData.buscarComRespostas(id).map(TopicoForumJpa::paraDominio);
    }

    @Override
    public Optional<TopicoDeForum> buscarPorIdParaAlteracao(UUID id) {
        // Bloquear a raiz sem JOIN evita bloquear a árvore; carregá-la depois lê o estado atualizado.
        if (springData.bloquearTopico(id).isEmpty()) {
            return Optional.empty();
        }
        return springData.buscarComRespostas(id).map(TopicoForumJpa::paraDominio);
    }

    @Override
    public Pagina<ResumoTopico> listar(FiltroTopicos filtro, int pagina, int tamanho) {
        String categoria = filtro.categoria() == null ? "" : filtro.categoria().trim();
        Page<TopicoForumJpa> resultado = springData.listar(filtro.jogoId(), categoria, filtro.apenasGerais(), filtro.status(), filtro.emAlta(),
                PageRequest.of(pagina - 1, tamanho));
        return new Pagina<>(resultado.map(TopicoForumJpa::paraResumo).getContent(), pagina, tamanho,
                resultado.getTotalElements());
    }
}
