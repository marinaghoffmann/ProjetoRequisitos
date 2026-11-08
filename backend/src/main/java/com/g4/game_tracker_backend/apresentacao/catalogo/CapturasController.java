package com.g4.game_tracker_backend.apresentacao.catalogo;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.catalogo.BuscadorDeCapturas.Captura;
import com.g4.game_tracker_backend.aplicacao.catalogo.ConsultarCapturasJogo;

@RestController
@RequestMapping("/api/catalogo/jogos")
public class CapturasController {

    private final ConsultarCapturasJogo consultar;

    public CapturasController(ConsultarCapturasJogo consultar) {
        this.consultar = consultar;
    }

    @GetMapping("/{id}/capturas")
    public List<Captura> capturas(@PathVariable UUID id) {
        return consultar.executar(id);
    }
}