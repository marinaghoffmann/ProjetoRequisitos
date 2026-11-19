package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.compartilhado.Pagina;
import com.g4.game_tracker_backend.dominio.moderacao.LogDeModeracao;
import com.g4.game_tracker_backend.dominio.moderacao.LogModeracaoRepositorio;

@Repository
class LogModeracaoRepositorioPostgres implements LogModeracaoRepositorio {

    private final LogModeracaoRepository springData;

    LogModeracaoRepositorioPostgres(LogModeracaoRepository springData) {
        this.springData = springData;
    }

    @Override
    public LogDeModeracao registrar(LogDeModeracao log) {
        return springData.save(LogModeracaoJpa.deDominio(log)).paraDominio();
    }

    @Override
    public Optional<LogDeModeracao> buscarPorId(UUID id) {
        return springData.findById(id).map(LogModeracaoJpa::paraDominio);
    }

    @Override
    public List<LogDeModeracao> listarPorConteudo(UUID conteudoId) {
        return springData.findByConteudoIdOrderByRegistradoEmAsc(conteudoId).stream()
                .map(LogModeracaoJpa::paraDominio).toList();
    }

    @Override
    public Pagina<LogDeModeracao> historico(int pagina, int tamanho) {
        Page<LogModeracaoJpa> resultado = springData.findAllByOrderByRegistradoEmDesc(
                PageRequest.of(pagina - 1, tamanho));
        return new Pagina<>(resultado.getContent().stream().map(LogModeracaoJpa::paraDominio).toList(), pagina,
                tamanho, resultado.getTotalElements());
    }
}
