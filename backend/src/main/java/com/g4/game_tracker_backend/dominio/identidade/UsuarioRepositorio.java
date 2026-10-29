package com.g4.game_tracker_backend.dominio.identidade;

import java.util.Optional;
import java.util.UUID;

/** Porta de persistência de usuários, sem dependência de Spring ou JPA. */
public interface UsuarioRepositorio {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorId(UUID id);

    Optional<Usuario> buscarPorEmail(String email);

    boolean existePorEmail(String email);
}
