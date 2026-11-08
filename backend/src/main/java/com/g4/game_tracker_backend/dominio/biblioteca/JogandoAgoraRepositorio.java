package com.g4.game_tracker_backend.dominio.biblioteca;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JogandoAgoraRepositorio {

    JogandoAgora salvar(JogandoAgora registro);

    List<JogandoAgora> buscarAtivos(UUID usuarioId);

    Optional<JogandoAgora> buscarAtivo(UUID usuarioId, UUID jogoId);

    long contarAtivos(UUID usuarioId);
}