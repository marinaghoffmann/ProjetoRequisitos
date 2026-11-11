package com.g4.game_tracker_backend.dominio.marketplace.pedidos;

import java.util.Locale;
import java.util.regex.Pattern;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/** F-20 RN 3: destinatário, CEP, rua, número, cidade e UF obrigatórios; complemento opcional. */
public record EnderecoEntrega(String destinatario, String cep, String rua, String numero, String complemento,
        String cidade, String uf) {

    private static final Pattern CEP = Pattern.compile("\\d{5}-\\d{3}");
    private static final Pattern UF = Pattern.compile("[A-Z]{2}");

    public EnderecoEntrega {
        destinatario = aparar(destinatario);
        if (destinatario == null || destinatario.length() < 3 || destinatario.length() > 80) {
            throw new RegraDeNegocioException("O destinatário deve ter de 3 a 80 caracteres");
        }
        cep = aparar(cep);
        if (cep == null || !CEP.matcher(cep).matches()) {
            throw new RegraDeNegocioException("O CEP deve estar no formato 00000-000");
        }
        rua = obrigatorio(rua, 120, "A rua é obrigatória", "A rua deve ter no máximo 120 caracteres");
        numero = obrigatorio(numero, 10, "O número é obrigatório", "O número deve ter no máximo 10 caracteres");
        complemento = aparar(complemento);
        if (complemento != null && complemento.length() > 60) {
            throw new RegraDeNegocioException("O complemento deve ter no máximo 60 caracteres");
        }
        cidade = obrigatorio(cidade, 80, "A cidade é obrigatória", "A cidade deve ter no máximo 80 caracteres");
        uf = aparar(uf);
        uf = uf == null ? null : uf.toUpperCase(Locale.ROOT);
        if (uf == null || !UF.matcher(uf).matches()) {
            throw new RegraDeNegocioException("A UF deve ter 2 letras");
        }
    }

    private static String aparar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }

    private static String obrigatorio(String valor, int limite, String mensagemObrigatorio, String mensagemLimite) {
        String aparado = aparar(valor);
        if (aparado == null) {
            throw new RegraDeNegocioException(mensagemObrigatorio);
        }
        if (aparado.length() > limite) {
            throw new RegraDeNegocioException(mensagemLimite);
        }
        return aparado;
    }
}
