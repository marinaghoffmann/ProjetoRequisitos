package com.g4.game_tracker_backend.dominio.catalogo;

import java.util.UUID;

/**
 * Evento de domínio publicado quando o administrador desativa um jogo (F-01 RN 3). Os contextos que dependem
 * do catálogo reagem a ele (por exemplo, a F-12 cancela os alertas do lançamento) sem que o catálogo os conheça.
 */
public record JogoDesativado(UUID jogoId) {
}
