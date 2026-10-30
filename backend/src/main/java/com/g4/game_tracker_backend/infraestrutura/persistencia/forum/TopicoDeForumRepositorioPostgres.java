package com.g4.game_tracker_backend.infraestrutura.persistencia.forum;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import com.g4.game_tracker_backend.dominio.forum.FiltroTopicos;
import com.g4.game_tracker_backend.dominio.forum.ResumoTopico;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForum;
import com.g4.game_tracker_backend.dominio.forum.TopicoDeForumRepositorio;

@Repository
public class TopicoDeForumRepositorioPostgres implements TopicoDeForumRepositorio {

    private static final Sort MAIS_ATIVOS_PRIMEIRO = Sort.by(Sort.Order.desc("dataUltimaAtividade"),
            Sort.Order.desc("id"));

    private final TopicoForumSpringDataRepository springData;

    public TopicoDeForumRepositorioPostgres(TopicoForumSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public TopicoDeForum salvar(TopicoDeForum topico) {
        springData.save(TopicoForumJpa.deDominio(topico));
        return topico;
    }

    @Override
    public Optional<TopicoDeForum> buscarPorId(UUID id) {
        return springData.buscarComRespostas(id).map(TopicoForumJpa::paraDominio);
    }

    @Override
    public Pagina<ResumoTopico> listar(FiltroTopicos filtro, int pagina, int tamanho) {
        String categoria = filtro.categoria() == null ? "" : filtro.categoria().trim();
        Page<TopicoForumJpa> resultado = springData.listar(filtro.jogoId(), categoria,
                PageRequest.of(pagina - 1, tamanho, MAIS_ATIVOS_PRIMEIRO));
        return new Pagina<>(resultado.map(TopicoForumJpa::paraResumo).getContent(), pagina, tamanho,
                resultado.getTotalElements());
    }
}
