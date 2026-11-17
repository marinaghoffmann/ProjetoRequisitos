package com.g4.game_tracker_backend.dominio.biblioteca.conclusoes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegistroZeradoNaBiblioteca(UUID id, UUID jogoId, String titulo, String capaUrl,
        LocalDate dataConclusao, String plataforma, BigDecimal horasJogadas, boolean concluido, boolean platina) {
}
