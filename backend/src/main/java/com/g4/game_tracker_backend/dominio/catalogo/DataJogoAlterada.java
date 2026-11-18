package com.g4.game_tracker_backend.dominio.catalogo;
import java.time.LocalDate;
import java.util.UUID;
public record DataJogoAlterada(UUID jogoId, LocalDate anterior, LocalDate nova, UUID alteradoPorId) {}
