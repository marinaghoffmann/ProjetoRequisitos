package com.g4.game_tracker_backend.infraestrutura.persistencia.estatisticas;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogo;
import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.EstatisticaJogoRepositorio;

@Repository
public class EstatisticaJogoRepositorioPostgres implements EstatisticaJogoRepositorio {

    private final EstatisticaJogoSpringDataRepository springData;

    public EstatisticaJogoRepositorioPostgres(EstatisticaJogoSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public EstatisticaJogo salvar(EstatisticaJogo estatistica) {
        springData.save(EstatisticaJogoJpa.deDominio(estatistica));
        return estatistica;
    }

    @Override
    public Optional<EstatisticaJogo> buscarPorJogo(UUID jogoId) {
        return springData.findById(jogoId).map(EstatisticaJogoJpa::paraDominio);
    }
}
