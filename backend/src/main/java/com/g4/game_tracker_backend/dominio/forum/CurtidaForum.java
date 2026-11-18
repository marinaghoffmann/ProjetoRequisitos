package com.g4.game_tracker_backend.dominio.forum;

import java.util.UUID;

public record CurtidaForum(UUID usuarioId, UUID topicoId, String tipo, UUID conteudoId) {}
