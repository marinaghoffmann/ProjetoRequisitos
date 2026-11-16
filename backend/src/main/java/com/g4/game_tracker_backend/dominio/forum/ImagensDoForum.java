package com.g4.game_tracker_backend.dominio.forum;

import java.util.ArrayList;
import java.util.List;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Regras das imagens anexadas a tópicos e respostas do fórum: no máximo {@link #LIMITE} por postagem e
 * somente arquivos enviados por POST /api/forum/imagens (URLs que começam com {@link #PREFIXO}).
 */
public final class ImagensDoForum {

    public static final int LIMITE = 4;
    public static final String PREFIXO = "/uploads/forum/";
    private static final int TAMANHO_MAXIMO_DA_URL = 300;

    private ImagensDoForum() {
    }

    /** Devolve a lista normalizada (sem repetidas); nulo ou vazio resultam em lista vazia. */
    public static List<String> validar(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return List.of();
        }
        if (urls.size() > LIMITE) {
            throw new RegraDeNegocioException("Anexe no máximo " + LIMITE + " imagens");
        }
        List<String> validas = new ArrayList<>();
        for (String url : urls) {
            String normalizada = url == null ? "" : url.strip();
            if (!valida(normalizada)) {
                throw new RegraDeNegocioException("Imagem inválida: anexe apenas imagens enviadas pelo fórum");
            }
            if (!validas.contains(normalizada)) {
                validas.add(normalizada);
            }
        }
        return List.copyOf(validas);
    }

    private static boolean valida(String url) {
        return url.startsWith(PREFIXO)
                && url.length() > PREFIXO.length()
                && url.length() <= TAMANHO_MAXIMO_DA_URL
                && !url.contains("..")
                && !url.contains("\\")
                && !url.contains("?")
                && !url.contains("#")
                && url.chars().noneMatch(Character::isWhitespace);
    }
}