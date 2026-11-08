package com.g4.game_tracker_backend.infraestrutura.persistencia.catalogo;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.g4.game_tracker_backend.aplicacao.catalogo.BuscadorDeCapturas;

@Component
public class SteamCapturas implements BuscadorDeCapturas {

    private final RestClient cliente;

    public SteamCapturas(@Value("${app.catalogo.steam-url:https://store.steampowered.com}") String url) {
        this.cliente = RestClient.builder().baseUrl(url).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Captura> buscar(String appId) {
        try {
            Map<String, Object> resposta = cliente.get()
                    .uri(u -> u.path("/api/appdetails").queryParam("appids", appId).build())
                    .retrieve().body(Map.class);
            Map<String, Object> envelope = resposta == null ? null : (Map<String, Object>) resposta.get(appId);
            Map<String, Object> dados = envelope == null ? null : (Map<String, Object>) envelope.get("data");
            if (dados == null || !(dados.get("screenshots") instanceof List<?> lista)) return List.of();
            return lista.stream()
                    .filter(i -> i instanceof Map<?, ?>)
                    .map(i -> (Map<?, ?>) i)
                    .filter(m -> m.get("path_thumbnail") != null && m.get("path_full") != null)
                    .map(m -> new Captura(String.valueOf(m.get("path_thumbnail")), String.valueOf(m.get("path_full"))))
                    .limit(8)
                    .toList();
        } catch (RestClientException e) {
            return List.of();
        }
    }
}