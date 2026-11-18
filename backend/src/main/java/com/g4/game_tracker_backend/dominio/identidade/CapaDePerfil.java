package com.g4.game_tracker_backend.dominio.identidade;

import java.net.URI;
import java.util.Locale;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

public final class CapaDePerfil {
    private CapaDePerfil() {
    }

    public static String validar(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String url = valor.strip();
        try {
            URI uri = URI.create(url);
            boolean externa = ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null && uri.getUserInfo() == null;
            boolean uploadLocal = url.startsWith("/uploads/perfil/") && !url.contains("..") && uri.getHost() == null;
            String caminho = uri.getPath().toLowerCase(Locale.ROOT);
            if ((!externa && !uploadLocal) || !caminho.matches(".*\\.(png|jpg|webp)$")) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RegraDeNegocioException("Use uma URL de capa válida com extensão .png, .jpg ou .webp");
        }
        return url;
    }
}
