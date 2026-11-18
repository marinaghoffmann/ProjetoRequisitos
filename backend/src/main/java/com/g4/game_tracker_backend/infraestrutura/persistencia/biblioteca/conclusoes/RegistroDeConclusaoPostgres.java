package com.g4.game_tracker_backend.infraestrutura.persistencia.biblioteca.conclusoes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroDeConclusao;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroDeConclusaoRepositorio;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.ResumoJogoZerado;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroZeradoNaBiblioteca;

import jakarta.persistence.EntityManager;

@Repository
class RegistroDeConclusaoPostgres implements RegistroDeConclusaoRepositorio {

    private final RegistroDeConclusaoSpringData dados;
    private final EntityManager entidades;

    RegistroDeConclusaoPostgres(RegistroDeConclusaoSpringData dados, EntityManager entidades) {
        this.dados = dados;
        this.entidades = entidades;
    }

    @Override
    public void inserir(RegistroDeConclusao registro) {
        entidades.persist(new RegistroDeConclusaoJpa(registro));
    }

    @Override
    public Optional<RegistroDeConclusao> buscarParaAlterar(UUID id) {
        return dados.findRegistroById(id).map(RegistroDeConclusaoJpa::paraDominio);
    }

    @Override
    public void atualizar(RegistroDeConclusao registro) {
        entidades.find(RegistroDeConclusaoJpa.class, registro.id()).atualizar(registro);
    }

    @Override
    public void excluir(UUID id) {
        dados.deleteById(id);
    }

    @Override
    public List<RegistroDeConclusao> historico(UUID jogadorId, UUID jogoId) {
        return dados.findByJogadorIdAndJogoIdOrderByDataConclusaoDescRegistradoEmDescIdDesc(jogadorId, jogoId)
                .stream().map(RegistroDeConclusaoJpa::paraDominio).toList();
    }

    @Override
    public List<ResumoJogoZerado> jogosZerados(UUID jogadorId) {
        return dados.resumirPorJogador(jogadorId);
    }

    @Override
    public List<RegistroZeradoNaBiblioteca> registrosDoJogador(UUID jogadorId) {
        return dados.detalharPorJogador(jogadorId);
    }

    @Override
    public int contarPorJogo(UUID jogoId) {
        return dados.countByJogoId(jogoId);
    }
}
