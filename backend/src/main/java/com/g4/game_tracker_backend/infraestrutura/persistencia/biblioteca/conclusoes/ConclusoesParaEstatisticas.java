package com.g4.game_tracker_backend.infraestrutura.persistencia.biblioteca.conclusoes;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.avaliacao.estatisticas.FonteDeConclusoes;
import com.g4.game_tracker_backend.dominio.biblioteca.conclusoes.RegistroDeConclusaoRepositorio;

@Component
class ConclusoesParaEstatisticas implements FonteDeConclusoes {

    private final RegistroDeConclusaoRepositorio registros;

    ConclusoesParaEstatisticas(RegistroDeConclusaoRepositorio registros) {
        this.registros = registros;
    }

    @Override
    public int contarConclusoes(UUID jogoId) {
        return registros.contarPorJogo(jogoId);
    }
}
