package com.g4.game_tracker_backend.apresentacao.biblioteca;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g4.game_tracker_backend.aplicacao.biblioteca.acervo.GerenciarBiblioteca;
import com.g4.game_tracker_backend.aplicacao.biblioteca.acervo.GerenciarBiblioteca.Item;

@RestController
@RequestMapping("/api/biblioteca")
public class BibliotecaController {

    private final GerenciarBiblioteca biblioteca;

    public BibliotecaController(GerenciarBiblioteca biblioteca) {
        this.biblioteca = biblioteca;
    }

    @GetMapping
    public List<Item> listar() {
        return biblioteca.listar();
    }

    @PostMapping("/{jogoId}")
    public ResponseEntity<Item> adicionar(@PathVariable UUID jogoId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(biblioteca.adicionar(jogoId));
    }

    @DeleteMapping("/{jogoId}")
    public ResponseEntity<Void> remover(@PathVariable UUID jogoId) {
        biblioteca.remover(jogoId);
        return ResponseEntity.noContent().build();
    }
}