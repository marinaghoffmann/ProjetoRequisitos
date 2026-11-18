package com.g4.game_tracker_backend.infraestrutura.persistencia.perfil;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.biblioteca.perfil.PerfilFavoritos;
import com.g4.game_tracker_backend.dominio.biblioteca.perfil.PerfilFavoritosRepositorio;
import com.g4.game_tracker_backend.dominio.compartilhado.RecursoNaoEncontradoException;
import com.g4.game_tracker_backend.infraestrutura.persistencia.identidade.UsuarioJpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

@Repository
class PerfilFavoritosPostgres implements PerfilFavoritosRepositorio {
    private final EntityManager entidades;

    PerfilFavoritosPostgres(EntityManager entidades) {
        this.entidades = entidades;
    }

    @Override
    public PerfilFavoritos buscar(UUID donoId) {
        var perfil = entidades.find(PerfilFavoritosJpa.class, donoId);
        return new PerfilFavoritos(donoId, perfil == null ? List.of() : perfil.jogos);
    }

    @Override
    public PerfilFavoritos buscarParaAlterar(UUID donoId) {
        // Serializa as operações mesmo quando ainda não existe uma lista de favoritos.
        if (entidades.find(UsuarioJpa.class, donoId, LockModeType.PESSIMISTIC_WRITE) == null) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado");
        }
        return buscar(donoId);
    }

    @Override
    public void salvar(PerfilFavoritos perfil) {
        var linha = entidades.find(PerfilFavoritosJpa.class, perfil.donoId());
        if (linha == null) {
            linha = new PerfilFavoritosJpa(perfil.donoId());
            entidades.persist(linha);
        }
        linha.jogos.clear();
        // Remove as posições antigas antes de gravar a nova ordem, respeitando a unicidade.
        entidades.flush();
        linha.jogos.addAll(perfil.jogos());
    }
}
