package com.g4.game_tracker_backend.dominio.biblioteca.perfil;

import java.util.UUID;

public interface PerfilFavoritosRepositorio {
    PerfilFavoritos buscar(UUID donoId);
    PerfilFavoritos buscarParaAlterar(UUID donoId);
    void salvar(PerfilFavoritos perfil);
}
