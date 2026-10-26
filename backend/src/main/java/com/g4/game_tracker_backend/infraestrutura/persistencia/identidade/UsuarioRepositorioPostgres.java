package com.g4.game_tracker_backend.infraestrutura.persistencia.identidade;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.g4.game_tracker_backend.dominio.identidade.Usuario;
import com.g4.game_tracker_backend.dominio.identidade.UsuarioRepositorio;

@Repository
public class UsuarioRepositorioPostgres implements UsuarioRepositorio {

    private final UsuarioSpringDataRepository springData;

    public UsuarioRepositorioPostgres(UsuarioSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return springData.save(UsuarioJpa.deDominio(usuario)).paraDominio();
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        return springData.findById(id).map(UsuarioJpa::paraDominio);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return springData.findByEmail(normalizarEmail(email)).map(UsuarioJpa::paraDominio);
    }

    @Override
    public boolean existePorEmail(String email) {
        return springData.existsByEmail(normalizarEmail(email));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
