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
    public static final long TAMANHO_MAXIMO_ARQUIVO = 5L * 1024 * 1024;
    public static final String PREFIXO = "/uploads/forum/";
    private static final int TAMANHO_MAXIMO_DA_URL = 300;

    private ImagensDoForum() {
    }

    /** Valida tamanho e assinatura real do arquivo; devolve a extensão confiável. */
    public static String validarArquivo(byte[] conteudo) {
        if (conteudo == null || conteudo.length == 0) {
            throw new RegraDeNegocioException("Selecione uma imagem para enviar");
        }
        if (conteudo.length > TAMANHO_MAXIMO_ARQUIVO) {
            throw new RegraDeNegocioException("A imagem deve ter no máximo 5 MB");
        }
        if (conteudo.length >= 8 && (conteudo[0] & 0xFF) == 0x89 && conteudo[1] == 'P'
                && conteudo[2] == 'N' && conteudo[3] == 'G' && conteudo[4] == 0x0D
                && conteudo[5] == 0x0A && conteudo[6] == 0x1A && conteudo[7] == 0x0A) return "png";
        if (conteudo.length >= 3 && (conteudo[0] & 0xFF) == 0xFF && (conteudo[1] & 0xFF) == 0xD8
                && (conteudo[2] & 0xFF) == 0xFF) return "jpg";
        if (conteudo.length >= 12 && conteudo[0] == 'R' && conteudo[1] == 'I'
                && conteudo[2] == 'F' && conteudo[3] == 'F' && conteudo[8] == 'W'
                && conteudo[9] == 'E' && conteudo[10] == 'B' && conteudo[11] == 'P') return "webp";
        throw new RegraDeNegocioException("Envie uma imagem PNG, JPG ou WEBP");
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