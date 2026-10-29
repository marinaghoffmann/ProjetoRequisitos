package com.g4.game_tracker_backend.infraestrutura.persistencia.catalogo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.aplicacao.catalogo.ArmazenadorDeImagem;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

@Component
public class ArmazenadorDeImagemLocal implements ArmazenadorDeImagem {

    private static final long TAMANHO_MAXIMO = 5 * 1024 * 1024;
    private static final Map<String, String> EXTENSOES = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "image/webp", ".webp");

    private final Path diretorio;

    public ArmazenadorDeImagemLocal(@Value("${app.uploads.catalogo-diretorio:uploads/catalogo}") String diretorio) {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
    }

    @Override
    public String armazenar(byte[] conteudo, String nomeOriginal, String tipoConteudo) {
        if (conteudo == null || conteudo.length == 0) {
            throw new RegraDeNegocioException("A imagem não pode estar vazia");
        }
        if (conteudo.length > TAMANHO_MAXIMO) {
            throw new RegraDeNegocioException("A imagem deve ter no máximo 5 MB");
        }
        String extensao = EXTENSOES.get(tipoConteudo == null ? "" : tipoConteudo.toLowerCase(Locale.ROOT));
        if (extensao == null || !nomeTemExtensaoPermitida(nomeOriginal, extensao)) {
            throw new RegraDeNegocioException("A imagem deve estar no formato PNG, JPG ou WEBP");
        }
        try {
            Files.createDirectories(diretorio);
            String nome = UUID.randomUUID() + extensao;
            Files.write(diretorio.resolve(nome), conteudo, StandardOpenOption.CREATE_NEW);
            return "/uploads/catalogo/" + nome;
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar a imagem do catálogo", e);
        }
    }

    private boolean nomeTemExtensaoPermitida(String nomeOriginal, String extensao) {
        return nomeOriginal != null && nomeOriginal.toLowerCase(Locale.ROOT).endsWith(extensao);
    }
}
