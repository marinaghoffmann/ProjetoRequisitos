package com.g4.game_tracker_backend.infraestrutura.persistencia.moderacao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.moderacao.EstadoConteudo;
import com.g4.game_tracker_backend.dominio.moderacao.EstadoConteudoRepositorio;
import com.g4.game_tracker_backend.dominio.moderacao.TipoConteudo;

@Repository
class EstadoConteudoRepositorioPostgres implements EstadoConteudoRepositorio {

    private final EstadoConteudoRepository springData;

    EstadoConteudoRepositorioPostgres(EstadoConteudoRepository springData) {
        this.springData = springData;
    }

    @Override
    public Optional<EstadoConteudo> buscar(TipoConteudo tipo, UUID conteudoId) {
        return springData.findByTipoAndConteudoId(tipo, conteudoId).map(EstadoConteudoJpa::paraDominio);
    }

    /** saveAndFlush: as recontagens do fórum, em SQL, leem a tabela logo em seguida. */
    @Override
    public EstadoConteudo salvar(EstadoConteudo estado) {
        EstadoConteudoJpa jpa = springData.findById(estado.id()).orElse(null);
        if (jpa == null) {
            jpa = EstadoConteudoJpa.deDominio(estado);
        } else {
            jpa.atualizar(estado);
        }
        return springData.saveAndFlush(jpa).paraDominio();
    }
}
