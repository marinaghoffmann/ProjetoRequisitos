package com.g4.game_tracker_backend.infraestrutura.armazenamento;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.identidade.ArmazenamentoDeImagem;

/** Grava as imagens de perfil em uploads/perfil/{avatar|banner}, servidas por /uploads/perfil/**. */
@Component
public class ArmazenamentoLocal implements ArmazenamentoDeImagem {

    private final Path raiz = Path.of("uploads", "perfil").toAbsolutePath().normalize();

    @Override
    public String salvar(String pasta, byte[] conteudo, String extensao) {
        try {
            Path destino = raiz.resolve(pasta).normalize();
            if (!destino.startsWith(raiz)) {
                throw new IllegalArgumentException("Pasta inválida");
            }
            Files.createDirectories(destino);
            String nome = UUID.randomUUID() + "." + extensao; // nunca usar o nome enviado pelo cliente
            Files.write(destino.resolve(nome), conteudo);
            return "/uploads/perfil/" + pasta + "/" + nome;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}