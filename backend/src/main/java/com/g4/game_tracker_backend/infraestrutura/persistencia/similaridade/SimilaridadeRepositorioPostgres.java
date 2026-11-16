package com.g4.game_tracker_backend.infraestrutura.persistencia.similaridade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeJogo;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.SimilaridadeRepositorio;

@Repository
@Transactional(readOnly = true)
public class SimilaridadeRepositorioPostgres implements SimilaridadeRepositorio {

    private final SimilaridadeSpringDataRepository springData;

    public SimilaridadeRepositorioPostgres(SimilaridadeSpringDataRepository springData) {
        this.springData = springData;
    }

    /** A linha existente é alterada no lugar, para a versão (bloqueio otimista) ser conferida. */
    @Override
    @Transactional
    public SimilaridadeJogo salvar(SimilaridadeJogo similaridade) {
        SimilaridadeJogoJpa jpa = springData.findById(similaridade.id())
                .map(existente -> {
                    existente.atualizarCom(similaridade);
                    return existente;
                })
                .orElseGet(() -> SimilaridadeJogoJpa.deDominio(similaridade));
        springData.saveAndFlush(jpa);
        return similaridade;
    }

    @Override
    public Optional<SimilaridadeJogo> buscarPorId(UUID id) {
        return springData.findById(id).map(SimilaridadeJogoJpa::paraDominio);
    }

    @Override
    public Optional<SimilaridadeJogo> buscarPar(UUID jogoUm, UUID jogoDois) {
        UUID[] par = SimilaridadeJogo.parNormalizado(jogoUm, jogoDois);
        return springData.findByJogoAIdAndJogoBId(par[0], par[1]).map(SimilaridadeJogoJpa::paraDominio);
    }

    @Override
    public List<SimilaridadeJogo> listarDoJogo(UUID jogoId) {
        return springData.listarDoJogo(jogoId).stream().map(SimilaridadeJogoJpa::paraDominio).toList();
    }

    @Override
    public boolean existeParaJogo(UUID jogoId) {
        return springData.existeParaJogo(jogoId);
    }
}
