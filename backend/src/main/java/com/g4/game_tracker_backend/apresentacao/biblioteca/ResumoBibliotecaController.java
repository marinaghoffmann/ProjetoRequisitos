package com.g4.game_tracker_backend.apresentacao.biblioteca;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.biblioteca.ResumirBiblioteca;

/** Contadores do menu lateral (Em andamento, Wishlist, Zerados) do usuário autenticado. */
@RestController
public class ResumoBibliotecaController {

    private final ResumirBiblioteca resumir;

    public ResumoBibliotecaController(ResumirBiblioteca resumir) {
        this.resumir = resumir;
    }

    @GetMapping("/api/perfis/me/resumo")
    public ResumirBiblioteca.Resumo resumo() {
        return resumir.executar();
    }
}
