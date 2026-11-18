package com.g4.game_tracker_backend.dominio.biblioteca.conclusoes;

import java.time.LocalDate;
import java.util.UUID;

public record ResumoJogoZerado(UUID jogoId, String titulo, long totalConclusoes, LocalDate ultimaConclusao) {
}
