package com.g4.game_tracker_backend.similaridade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.g4.game_tracker_backend.dominio.catalogo.FiltroCatalogo;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;
import com.g4.game_tracker_backend.dominio.descoberta.similaridade.PerfilJogo;
import com.g4.game_tracker_backend.infraestrutura.persistencia.similaridade.PerfilJogoDoCatalogo;

import tools.jackson.databind.json.JsonMapper;

class PerfilJogoDoCatalogoTest {

    private final UUID estudio = UUID.randomUUID();
    private final UUID genero = UUID.randomUUID();

    private PerfilJogoDoCatalogo gateway(Jogo... jogos) {
        JogoRepositorio repositorio = new JogoRepositorio() {
            @Override
            public Jogo salvar(Jogo jogo) {
                return jogo;
            }

            @Override
            public Optional<Jogo> buscarPorId(UUID id) {
                return List.of(jogos).stream().filter(jogo -> jogo.id().equals(id)).findFirst();
            }

            @Override
            public Optional<Jogo> buscarPorIdentificadorExterno(String id) {
                return Optional.empty();
            }

            @Override
            public boolean existePorTituloEDesenvolvedora(String titulo, UUID desenvolvedoraId) {
                return false;
            }

            @Override
            public boolean existePorTituloEDesenvolvedoraEIdDiferente(String titulo, UUID d, UUID j) {
                return false;
            }

            @Override
            public List<Jogo> consultar(FiltroCatalogo filtro) {
                return List.of(jogos);
            }
        };
        return new PerfilJogoDoCatalogo(repositorio, JsonMapper.builder().build());
    }

    private Jogo jogo(String titulo, String metadados) {
        return Jogo.criarManual(titulo, null, null, estudio, Set.of(genero), null, null, null).comMetadados(metadados);
    }

    @Test
    void montaTagsDosDescritoresDoMetadados() {
        Jogo hades = jogo("Hades", """
                {"tags":["Roguelike"],"subgeneros":["Ação "],"temas":["Mitologia"],"modosJogo":["Single-player"],
                 "generoPrincipal":"Rogue-lite","publicadora":"Ignorada"}""");
        PerfilJogo perfil = gateway(hades).obterPerfil(hades.id()).orElseThrow();
        assertEquals(Set.of("roguelike", "ação", "mitologia", "single-player", "rogue-lite"), perfil.tags());
        assertEquals(Set.of(genero.toString()), perfil.generos());
        assertEquals(estudio, perfil.desenvolvedoraId());
    }

    @Test
    void metadadosAusentesOuInvalidosResultamEmJogoSemTags() {
        Jogo semMetadados = jogo("Celeste", "");
        Jogo invalido = jogo("Tetris", "{nao e json");
        PerfilJogoDoCatalogo gateway = gateway(semMetadados, invalido);
        assertTrue(gateway.obterPerfil(semMetadados.id()).orElseThrow().tags().isEmpty());
        assertTrue(gateway.obterPerfil(invalido.id()).orElseThrow().tags().isEmpty());
        assertEquals(1, gateway.listarCandidatos(semMetadados.id()).size());
    }
}
