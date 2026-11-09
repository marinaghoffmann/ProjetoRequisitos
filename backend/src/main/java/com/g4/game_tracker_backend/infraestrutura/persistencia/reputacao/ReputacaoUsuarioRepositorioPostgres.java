package com.g4.game_tracker_backend.infraestrutura.persistencia.reputacao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.marketplace.reputacao.ReputacaoUsuario;
import com.g4.game_tracker_backend.dominio.marketplace.reputacao.ReputacaoUsuarioRepositorio;

@Repository
public class ReputacaoUsuarioRepositorioPostgres implements ReputacaoUsuarioRepositorio {

    private final ReputacaoUsuarioSpringDataRepository springData;

    public ReputacaoUsuarioRepositorioPostgres(ReputacaoUsuarioSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public ReputacaoUsuario salvar(ReputacaoUsuario reputacao) {
        springData.save(ReputacaoUsuarioJpa.deDominio(reputacao));
        return reputacao;
    }

    @Override
    public Optional<ReputacaoUsuario> buscarPorUsuario(UUID usuarioId) {
        return springData.findById(usuarioId).map(ReputacaoUsuarioJpa::paraDominio);
    }

    @Override
    public Optional<ReputacaoUsuario> buscarPorUsuarioParaAtualizar(UUID usuarioId) {
        return springData.buscarParaAtualizar(usuarioId).map(ReputacaoUsuarioJpa::paraDominio);
    }
}
