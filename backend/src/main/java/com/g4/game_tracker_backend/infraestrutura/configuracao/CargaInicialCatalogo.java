package com.g4.game_tracker_backend.infraestrutura.configuracao;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.g4.game_tracker_backend.dominio.catalogo.Desenvolvedora;
import com.g4.game_tracker_backend.dominio.catalogo.DesenvolvedoraRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Genero;
import com.g4.game_tracker_backend.dominio.catalogo.GeneroRepositorio;
import com.g4.game_tracker_backend.dominio.catalogo.Jogo;
import com.g4.game_tracker_backend.dominio.catalogo.JogoRepositorio;

@Configuration
public class CargaInicialCatalogo {

    @Bean
    CommandLineRunner carregarCatalogo(DesenvolvedoraRepositorio desenvolvedoras, GeneroRepositorio generos,
            JogoRepositorio jogos) {
        return args -> {
            Desenvolvedora estudio = desenvolvedoras.buscarPorNome("Umbra Interactive")
                    .orElseGet(() -> desenvolvedoras.salvar(Desenvolvedora.criar(
                            "Umbra Interactive", "Estúdio independente de jogos autorais.",
                            "https://umbra.example", "@umbrainteractive", "Brasil")));
            Genero rpg = generos.buscarPorNome("RPG")
                    .orElseGet(() -> generos.salvar(Genero.criar("RPG")));
            Genero acao = generos.buscarPorNome("Ação")
                    .orElseGet(() -> generos.salvar(Genero.criar("Ação")));
            Desenvolvedora fromSoftware = desenvolvedoras.buscarPorNome("FromSoftware")
                    .orElseGet(() -> desenvolvedoras.salvar(Desenvolvedora.criar(
                            "FromSoftware", "Estúdio japonês responsável por Elden Ring.",
                            "https://www.fromsoftware.jp", "@fromsoftware_pr", "Japão")));
            if (jogos.consultar(com.g4.game_tracker_backend.dominio.catalogo.FiltroCatalogo.ativos()).isEmpty()) {
                jogos.salvar(Jogo.importar("Eclipse: Fallen Order", null,
                        "Uma aventura épica em um universo em ruínas.", "SEED-ECLIPSE",
                        estudio.id(), Set.of(rpg.id()), LocalDate.of(2025, 6, 12),
                        "", ""));
                jogos.salvar(Jogo.criarManual("Neon Drifter", null,
                "Ação e velocidade em uma cidade neon.", estudio.id(), Set.of(acao.id()),
                        LocalDate.of(2026, 2, 20), "", ""));
            }
            if (jogos.buscarPorIdentificadorExterno("SEED-ELDEN-RING").isEmpty()) {
                jogos.salvar(Jogo.importar("Elden Ring", "ELDEN RING",
                        "Um RPG de ação em um mundo aberto vasto e interconectado.",
                        "SEED-ELDEN-RING", fromSoftware.id(), Set.of(rpg.id()),
                        LocalDate.of(2022, 2, 25), "", ""));
            }
        };
    }
}
