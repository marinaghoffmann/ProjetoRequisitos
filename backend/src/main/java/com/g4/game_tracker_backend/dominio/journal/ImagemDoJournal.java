package com.g4.game_tracker_backend.dominio.journal;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

public record ImagemDoJournal(String nomeArquivo, String tipoMidia, byte[] conteudo) {
    public static final int TAMANHO_MAXIMO = 2 * 1024 * 1024;
    private static final Set<String> TIPOS = Set.of("image/png", "image/jpeg", "image/webp");

    public ImagemDoJournal {
        if (conteudo == null || conteudo.length == 0 || conteudo.length > TAMANHO_MAXIMO) {
            throw new RegraDeNegocioException("Cada imagem deve ter conteúdo e no máximo 2 MB");
        }
        if (tipoMidia == null || !TIPOS.contains(tipoMidia) || !assinaturaValida(tipoMidia, conteudo)) {
            throw new RegraDeNegocioException("Envie uma imagem PNG, JPG ou WebP válida");
        }
        nomeArquivo = nomeArquivo == null ? "imagem" : nomeArquivo.replace('\\', '/');
        nomeArquivo = nomeArquivo.substring(nomeArquivo.lastIndexOf('/') + 1);
        if (nomeArquivo.isBlank()) nomeArquivo = "imagem";
        if (nomeArquivo.length() > 120) nomeArquivo = nomeArquivo.substring(nomeArquivo.length() - 120);
        conteudo = conteudo.clone();
    }

    @Override
    public byte[] conteudo() { return conteudo.clone(); }

    public AnexoJournal metadados() {
        return new AnexoJournal(UUID.randomUUID(), nomeArquivo, tipoMidia, conteudo.length);
    }

    private static boolean assinaturaValida(String tipo, byte[] bytes) {
        return switch (tipo) {
            case "image/png" -> bytes.length >= 8 && Arrays.equals(Arrays.copyOf(bytes, 8),
                    new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10});
            case "image/jpeg" -> bytes.length >= 3 && bytes[0] == (byte) 255 && bytes[1] == (byte) 216
                    && bytes[2] == (byte) 255;
            case "image/webp" -> bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F'
                    && bytes[3] == 'F' && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
            default -> false;
        };
    }
}
