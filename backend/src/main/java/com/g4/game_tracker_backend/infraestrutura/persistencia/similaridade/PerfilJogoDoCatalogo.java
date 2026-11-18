package com.g4.game_tracker_backend.infraestrutura.persistencia.similaridade;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.g4.game_tracker_backend.dominio.catalogo.FiltroCatalogo;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.StatusCatalogo;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.PerfilJogo;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.PerfilJogoGateway;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Anticorruption layer (ACL) da F-10 para o catálogo (F-01): monta o PerfilJogo a partir do Jogo sem mudar o
 * modelo do catálogo.
 *
 * - Gêneros: os gêneros cadastrados do jogo (generosIds).
 * - Estúdio: a desenvolvedora do jogo.
 * - Tags: o Jogo não tem um campo de tags, mas o cadastro e a edição manuais já gravam descritores em
 *   listas no metadadosJson. São usadas as chaves "tags" (para importações ou cargas que a informem),
 *   "subgeneros", "temas" e "modosJogo", além de "generoPrincipal", em minúsculas e sem espaços nas pontas.
 *   Metadados ausentes ou inválidos resultam em um jogo sem tags.
 */
@Component
public class PerfilJogoDoCatalogo implements PerfilJogoGateway {

    static final List<String> CHAVES_DE_TAGS = List.of("tags", "subgeneros", "temas", "modosJogo",
            "generoPrincipal");

    private static final Logger log = LoggerFactory.getLogger(PerfilJogoDoCatalogo.class);

    private final JogoRepositorio jogos;
    private final ObjectMapper objectMapper;

    public PerfilJogoDoCatalogo(JogoRepositorio jogos, ObjectMapper objectMapper) {
        this.jogos = jogos;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<PerfilJogo> obterPerfil(UUID jogoId) {
        return jogos.buscarPorId(jogoId)
                .filter(jogo -> jogo.status() == StatusCatalogo.ATIVO)
                .map(this::perfil);
    }

    @Override
    public List<PerfilJogo> listarCandidatos(UUID jogoId) {
        return jogos.consultar(FiltroCatalogo.ativos()).stream()
                .filter(jogo -> !jogo.id().equals(jogoId))
                .map(this::perfil)
                .toList();
    }

    private PerfilJogo perfil(Jogo jogo) {
        Set<String> generos = jogo.generosIds().stream().map(UUID::toString).collect(Collectors.toSet());
        return new PerfilJogo(jogo.id(), generos, tags(jogo), jogo.desenvolvedoraId());
    }

    private Set<String> tags(Jogo jogo) {
        Set<String> tags = new LinkedHashSet<>();
        String json = jogo.metadadosJson();
        if (json == null || json.isBlank()) {
            return tags;
        }
        try {
            JsonNode metadados = objectMapper.readTree(json);
            for (String chave : CHAVES_DE_TAGS) {
                JsonNode valor = metadados.get(chave);
                if (valor == null) {
                    continue;
                }
                if (valor.isArray()) {
                    valor.forEach(item -> adicionar(tags, item.asString()));
                } else if (valor.isString()) {
                    for (String parte : valor.asString().split(",")) {
                        adicionar(tags, parte);
                    }
                }
            }
        } catch (JacksonException e) {
            log.warn("Metadados inválidos no jogo {}; seguindo sem tags", jogo.id());
        }
        return tags;
    }

    private static void adicionar(Set<String> tags, String valor) {
        if (valor != null && !valor.isBlank()) {
            tags.add(valor.strip().toLowerCase(Locale.ROOT));
        }
    }
}
