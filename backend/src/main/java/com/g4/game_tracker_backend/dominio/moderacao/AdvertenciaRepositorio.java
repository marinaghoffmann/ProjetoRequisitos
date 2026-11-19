package com.g4.game_tracker_backend.dominio.moderacao;

import java.util.List;
import java.util.UUID;

public interface AdvertenciaRepositorio {
    Advertencia salvar(Advertencia advertencia);
    List<Advertencia> listarPorUsuario(UUID usuarioId);
    long contarPorUsuario(UUID usuarioId);
}
