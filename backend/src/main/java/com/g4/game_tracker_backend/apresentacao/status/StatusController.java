package com.g4.game_tracker_backend.apresentacao.status;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.status.ConsultarStatusDoSistema;
import com.g4.game_tracker_backend.aplicacao.status.ConsultarStatusDoSistema.StatusDoSistema;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final ConsultarStatusDoSistema consultarStatus;

    public StatusController(ConsultarStatusDoSistema consultarStatus) {
        this.consultarStatus = consultarStatus;
    }

    @GetMapping
    public StatusDoSistema consultar() {
        return consultarStatus.executar();
    }
}
