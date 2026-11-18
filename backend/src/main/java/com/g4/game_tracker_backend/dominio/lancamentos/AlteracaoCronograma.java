package com.g4.game_tracker_backend.dominio.lancamentos;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
public record AlteracaoCronograma(UUID id, LocalDate dataAnterior, LocalDate novaData,
        UUID alteradoPorId, Instant alteradoEm) {}
