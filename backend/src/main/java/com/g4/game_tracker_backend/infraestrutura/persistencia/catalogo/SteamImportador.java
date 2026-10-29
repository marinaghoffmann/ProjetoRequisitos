package com.g4.game_tracker_backend.infraestrutura.persistencia.catalogo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.g4.game_tracker_backend.aplicacao.catalogo.ImportadorJogoExterno;
import com.g4.game_tracker_backend.dominio.compartilhado.RegraDeNegocioException;

@Component
public class SteamImportador implements ImportadorJogoExterno {

    private static final DateTimeFormatter DATA_STEAM_INGLES =
            DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATA_STEAM_PORTUGUES = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("d/MMM./yyyy")
            .toFormatter(Locale.forLanguageTag("pt-BR"));

    private final RestClient cliente;

    public SteamImportador(@Value("${app.catalogo.steam-url:https://store.steampowered.com}") String url) {
        this.cliente = RestClient.builder().baseUrl(url).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public DadosJogoExterno buscar(String termo) {
        Map<String, Object> busca;
        try {
            busca = cliente.get()
                    .uri(uri -> uri.path("/api/storesearch/")
                            .queryParam("term", termo)
                            .queryParam("l", "english")
                            .queryParam("cc", "br")
                            .build())
                    .retrieve().body(Map.class);
        } catch (RestClientException e) {
            throw new RegraDeNegocioException("Não foi possível consultar a Steam agora");
        }
        List<Map<String, Object>> itens = busca == null
                ? List.of()
                : (List<Map<String, Object>>) busca.getOrDefault("items", List.of());
        if (itens.isEmpty() || itens.get(0).get("id") == null) {
            throw new RegraDeNegocioException("Jogo não encontrado na Steam");
        }

        String appId = String.valueOf(itens.get(0).get("id"));
        Map<String, Object> resposta;
        try {
            resposta = cliente.get()
                    .uri(uri -> uri.path("/api/appdetails")
                            .queryParam("appids", appId)
                            .queryParam("l", "english")
                            .queryParam("cc", "br")
                            .build())
                    .retrieve().body(Map.class);
        } catch (RestClientException e) {
            throw new RegraDeNegocioException("Não foi possível consultar os detalhes do jogo na Steam");
        }
        Map<String, Object> envelope = resposta == null ? Map.of() : (Map<String, Object>) resposta.get(appId);
        Map<String, Object> jogo = envelope == null ? Map.of() : (Map<String, Object>) envelope.get("data");
        if (!Boolean.TRUE.equals(envelope == null ? false : envelope.get("success")) || jogo == null
                || jogo.get("name") == null) {
            throw new RegraDeNegocioException("Não foi possível obter os detalhes do jogo na Steam");
        }

        return new DadosJogoExterno(
                appId,
                texto(jogo.get("name")),
                texto(jogo.get("name")),
                limitarResumo(texto(jogo.get("short_description"))),
                data(jogo.get("release_date")),
                texto(jogo.get("header_image")),
                nomes(jogo.get("developers"), "name"),
                nomes(jogo.get("genres"), "description"));
    }

    private String texto(Object valor) {
        return valor == null ? "" : String.valueOf(valor);
    }

    private String limitarResumo(String resumo) {
        return resumo.length() <= 600 ? resumo : resumo.substring(0, 597).stripTrailing() + "...";
    }

    private LocalDate data(Object valor) {
        if (!(valor instanceof Map<?, ?> dados)) return null;
        String data = texto(dados.get("date"));
        if (data.isBlank() || "coming soon".equalsIgnoreCase(data)) return null;
        for (DateTimeFormatter formato : List.of(DATA_STEAM_INGLES, DATA_STEAM_PORTUGUES)) {
            try {
                return LocalDate.parse(data, formato);
            } catch (DateTimeParseException ignorado) {
                // Tenta o formato alternativo retornado pela Steam.
            }
        }
        throw new RegraDeNegocioException("A data de lançamento retornada pela Steam é inválida");
    }

    private List<String> nomes(Object valores, String campo) {
        if (!(valores instanceof List<?> lista)) return List.of();

        return lista.stream()
                .map(item -> item instanceof Map<?, ?> mapa ? texto(mapa.get(campo)) : texto(item))
                .filter(nome -> !nome.isBlank())
                .toList();
    }
}
