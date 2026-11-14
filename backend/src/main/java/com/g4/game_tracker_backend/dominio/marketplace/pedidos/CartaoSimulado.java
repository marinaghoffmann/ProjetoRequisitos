package com.g4.game_tracker_backend.dominio.marketplace.pedidos;

import java.time.YearMonth;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

/**
 * Dados do cartão fictício (F-20 RN 5). São validados e descartados: o número nunca é gravado, só os quatro
 * últimos dígitos ficam no pagamento.
 */
public record CartaoSimulado(String numero, String nomeImpresso, String validade, String cvv) {

    private static final Pattern VALIDADE = Pattern.compile("(0[1-9]|1[0-2])/(\\d{2})");
    private static final Pattern CVV = Pattern.compile("\\d{3,4}");

    /** Confere número (13 a 19 dígitos pelo algoritmo de Luhn), validade não vencida e CVV; devolve o final. */
    public String validar(YearMonth mesAtual) {
        String digitos = numero == null ? "" : numero.replaceAll("[\\s-]", "");
        if (!digitos.matches("\\d{13,19}") || !luhn(digitos)) {
            throw new RegraDeNegocioException("Número do cartão inválido");
        }
        if (nomeImpresso == null || nomeImpresso.isBlank()) {
            throw new RegraDeNegocioException("O nome impresso no cartão é obrigatório");
        }
        Matcher mmaa = VALIDADE.matcher(validade == null ? "" : validade.strip());
        if (!mmaa.matches()) {
            throw new RegraDeNegocioException("A validade do cartão deve estar no formato MM/AA");
        }
        YearMonth vencimento = YearMonth.of(2000 + Integer.parseInt(mmaa.group(2)), Integer.parseInt(mmaa.group(1)));
        if (vencimento.isBefore(mesAtual)) {
            throw new RegraDeNegocioException("Cartão vencido");
        }
        if (cvv == null || !CVV.matcher(cvv.strip()).matches()) {
            throw new RegraDeNegocioException("O CVV deve ter 3 ou 4 dígitos");
        }
        return digitos.substring(digitos.length() - 4);
    }

    static boolean luhn(String digitos) {
        int soma = 0;
        boolean dobrar = false;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            int digito = digitos.charAt(i) - '0';
            if (dobrar) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            soma += digito;
            dobrar = !dobrar;
        }
        return soma % 10 == 0;
    }

    /** O número completo não aparece em logs nem em mensagens. */
    @Override
    public String toString() {
        return "CartaoSimulado[final=" + (numero == null || numero.length() < 4
                ? "?" : numero.substring(numero.length() - 4)) + "]";
    }
}
