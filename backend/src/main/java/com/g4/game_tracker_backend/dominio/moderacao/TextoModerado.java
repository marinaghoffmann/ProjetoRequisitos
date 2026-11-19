package com.g4.game_tracker_backend.dominio.moderacao;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/** Validação comum dos textos informados na moderação (motivo, justificativa, conteúdo editado). */
public final class TextoModerado {

    private TextoModerado() {
    }

    public static String exigir(String valor, int limite, String mensagemSeVazio) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException(mensagemSeVazio);
        }
        String texto = valor.strip();
        if (texto.length() > limite) {
            throw new RegraDeNegocioException("Texto acima do limite de " + limite + " caracteres");
        }
        return texto;
    }
}
