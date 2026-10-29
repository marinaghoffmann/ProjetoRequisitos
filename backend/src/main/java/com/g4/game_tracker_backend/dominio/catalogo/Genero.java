package com.g4.game_tracker_backend.dominio.catalogo;

import java.util.Objects;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

public final class Genero {

    private final UUID id;
    private final String nome;

    private Genero(UUID id, String nome) {
        this.id = Objects.requireNonNull(id, "O identificador do gênero é obrigatório");
        this.nome = texto(nome, "O nome do gênero é obrigatório");
    }

    public static Genero criar(String nome) {
        return new Genero(UUID.randomUUID(), nome);
    }

    public static Genero reconstituir(UUID id, String nome) {
        return new Genero(id, nome);
    }

    private static String texto(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException(mensagem);
        }
        return valor.trim();
    }

    public UUID id() {
        return id;
    }

    public String nome() {
        return nome;
    }
}
